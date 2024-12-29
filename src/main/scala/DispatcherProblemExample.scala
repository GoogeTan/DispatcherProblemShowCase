package me.katze

import cats.effect.*
import cats.effect.std.Dispatcher
import org.lwjgl.glfw.*
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.{GL, GLUtil}
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.system.MemoryUtil.NULL
import org.lwjgl.system.{Callback, MemoryStack, Platform}

import java.nio.IntBuffer
import java.util.Objects
import scala.concurrent.duration.Duration

object DispatcherProblemExample extends IOApp:
  override def run(args: List[String]): IO[ExitCode] =
    Dispatcher
      .parallel[IO]
      .use(main)
      .evalOn(MainThread)
      .map(_ => ExitCode.Success)
  end run

  def main(dispatcher : Dispatcher[IO]) : IO[Unit] =
    IO:
      GLFWErrorCallback.createPrint.set
      if !glfwInit() then
        throw IllegalStateException("Unable to initialize GLFW")
      end if
      glfwDefaultWindowHints()
      glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
      glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)
      glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE)
      val monitor = glfwGetPrimaryMonitor
      val (scaleX, scaleY) = monitorContentScale(monitor)
      val windowWidth = 800 * scaleX.round
      val windowHeight = 600 * scaleY.round
      val window = glfwCreateWindow(windowWidth, windowHeight, "My amazing wingow", NULL, NULL)
      if window == NULL then
        throw new RuntimeException("Failed to create the GLFW window")
      end if
      val vidmode = Objects.requireNonNull(glfwGetVideoMode(monitor))
      glfwSetWindowPos(window, (vidmode.width - windowWidth) / 2, (vidmode.height - windowHeight) / 2)
      glfwMakeContextCurrent(window)
      GL.createCapabilities
      val debugProc = GLUtil.setupDebugMessageCallback
      glfwSwapInterval(1)
      glfwShowWindow(window)
      glfwInvoke(window, windowSizeChanged(scaleX, scaleY), framebufferSizeChanged)
      glfwSetWindowSizeCallback(window, windowSizeChanged(scaleX, scaleY))
      glfwSetFramebufferSizeCallback(window, framebufferSizeChanged)
      glfwSetKeyCallback(window, (window: Long, key: Int, _: Int, action: Int, _: Int) =>
        println(s"Print from impure code. Key pressed $key")
        dispatcher.unsafeRunTimed(
          IO.println(s"Print from monad IO. Action preformed: $action"),
          Duration("1s")
        )
      )
      while !glfwWindowShouldClose(window) do
        glfwPollEvents()
      end while
  end main

  def framebufferSizeChanged(window: Long, width: Int, height: Int): Unit =
    glViewport(0, 0, width, height)
  end framebufferSizeChanged

  def windowSizeChanged(contentScaleX : Float, contentScaleY : Float)(window: Long, width: Int, height: Int): Unit =
    glMatrixMode(GL_PROJECTION)
    glLoadIdentity()
    if Platform.get ne Platform.MACOSX then
      glOrtho(0.0, width / contentScaleX, height / contentScaleY, 0.0, -1.0, 1.0)
    else
      glOrtho(0.0, width, height, 0.0, -1.0, 1.0)
    end if
    glMatrixMode(GL_MODELVIEW)
  end windowSizeChanged

  def monitorContentScale(monitor : Long) : (Float, Float) =
    val s = stackPush
    try
      val px = s.mallocFloat(1)
      val py = s.mallocFloat(1)
      glfwGetMonitorContentScale(monitor, px, py)
      (px.get(0), py.get(0))
    finally
      s.close()
    end try
  end monitorContentScale

  def glfwInvoke(window: Long, windowSizeCB: GLFWWindowSizeCallbackI, framebufferSizeCB: GLFWFramebufferSizeCallbackI): Unit = 
    val stack = stackPush
    try
      val w = stack.mallocInt(1)
      val h = stack.mallocInt(1)
      if (windowSizeCB != null) {
        glfwGetWindowSize(window, w, h)
        windowSizeCB.invoke(window, w.get(0), h.get(0))
      }
      if (framebufferSizeCB != null) {
        glfwGetFramebufferSize(window, w, h)
        framebufferSizeCB.invoke(window, w.get(0), h.get(0))
      }
    finally 
      if (stack != null) stack.close()
    end try  
  end glfwInvoke    
end DispatcherProblemExample
