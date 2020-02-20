val http4sVersion = "0.21.1"
val fs2Version = "2.2.2"
val catsVersion = "2.1.0"
val catsEffectVersion = "2.1.0"
val scalatestVersion = "3.1.0"

// Global / onLoad := (Global / onLoad).value.andThen { s =>
//   dynverAssertTagVersion.value
//   s
// }

ThisBuild / scalaVersion     := "2.13.1"
ThisBuild / crossScalaVersions += "2.12.10"
ThisBuild / organization     := "com.github.filosganga"
ThisBuild / organizationName := "Filippo De Luca"
ThisBuild / dynverSeparator  := "-"


lazy val root = (project in file("."))
  .settings(
    name := "dataduggee",
    scalacOptions -= "-Xfatal-warnings",  // enable all options from sbt-tpolecat except fatal warnings
    scalacOptions += "-target:jvm-1.9",
    libraryDependencies ++= List(
      "org.typelevel" %% "cats-macros" % catsVersion,
      "org.typelevel" %% "cats-kernel" % catsVersion,
      "org.typelevel" %% "cats-core" % catsVersion,
      "org.typelevel" %% "cats-effect" % catsEffectVersion,
      "co.fs2" %% "fs2-core" % fs2Version,
      "co.fs2" %% "fs2-io" % fs2Version,
      "org.http4s" %% "http4s-core" % http4sVersion,
      "org.http4s" %% "http4s-client" % http4sVersion,
      "org.http4s" %% "http4s-blaze-core" % http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % http4sVersion,
      "org.scalatest" %% "scalatest" % scalatestVersion % Test,
    )
  )
