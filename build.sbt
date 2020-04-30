val http4sVersion = "0.21.1"
val fs2Version = "2.2.2"
val catsVersion = "2.1.0"
val catsEffectVersion = "2.1.0"
val scalatestVersion = "3.1.0"
val scalacheckVersion = "1.14.3"
val scalatestScalacheckVersion = "3.1.0.1"
val circeVersion = "0.13.0"

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
    scalacOptions += "-target:jvm-1.8",
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
      "org.scalacheck" %% "scalacheck" % scalacheckVersion % Test,
      "org.scalatestplus" %% "scalacheck-1-14" % scalatestScalacheckVersion % Test,
      "io.circe" %% "circe-parser" % circeVersion % Test,
      "io.circe" %% "circe-core" % circeVersion % Test,
      ),
    commonSettings,
    releaseOptions
  )

lazy val commonSettings = Seq(
  startYear := Some(2019),
  licenses := Seq(
    "Apache-2.0" -> url("https://opensource.org/licenses/apache-2.0")
  ),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/ovotech/dataduggee"),
      "scm:git:git@github.com:ovotech/dataduggee.git"
    )
  )
)

lazy val releaseOptions = Seq(
  releaseEarlyWith := BintrayPublisher,
  releaseEarlyEnableSyncToMaven := false,
  releaseEarlyNoGpg := true,
  bintrayOrganization := Some("ovotech"),
  bintrayRepository := "maven",
  bintrayPackageLabels := Seq(
    "cats",
    "fs2",
    "scala",
    "datadog"
  ),
  version ~= (_.replace('+', '-')),
  dynver ~= (_.replace('+', '-'))
)
