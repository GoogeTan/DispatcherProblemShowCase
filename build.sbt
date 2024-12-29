
ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.6.2"

lazy val lwjglVersion = "3.3.5"
lazy val os = Option(System.getProperty("os.name", ""))
  .map(_.substring(0, 3).toLowerCase) match {
  case Some("win") => "windows"
  case Some("mac") => "macos"
  case _           => "linux"
}

lazy val root = (project in file("."))
  .settings(
    name := "DispatcherProblemShowCase",
    idePackagePrefix := Some("me.katze"),
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.12.0",
      "org.typelevel" %% "cats-effect" % "3.5.7",

      "org.lwjgl" % "lwjgl"        % lwjglVersion,
      "org.lwjgl" % "lwjgl-opengl" % lwjglVersion,
      "org.lwjgl" % "lwjgl-glfw"   % lwjglVersion,
      "org.lwjgl" % "lwjgl-stb"    % lwjglVersion,
      "org.lwjgl" % "lwjgl-assimp" % lwjglVersion,
      "org.lwjgl" % "lwjgl-nanovg" % lwjglVersion,
      "org.lwjgl" % "lwjgl"        % lwjglVersion classifier s"natives-$os",
      "org.lwjgl" % "lwjgl-opengl" % lwjglVersion classifier s"natives-$os",
      "org.lwjgl" % "lwjgl-glfw"   % lwjglVersion classifier s"natives-$os",
      "org.lwjgl" % "lwjgl-stb"    % lwjglVersion classifier s"natives-$os",
      "org.lwjgl" % "lwjgl-assimp" % lwjglVersion classifier s"natives-$os",
      "org.lwjgl" % "lwjgl-nanovg" % lwjglVersion classifier s"natives-$os"
    ),
  )
