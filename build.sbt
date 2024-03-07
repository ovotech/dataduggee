val http4sVersion = "0.23.25"

val http4sBlazeClientVersion = "0.23.16"

val fs2Version = "3.9.4"

val catsVersion = "2.1.1"

val catsEffectVersion = "3.5.3"

val scalatestVersion = "3.1.0"

val scalacheckVersion = "1.14.3"

val scalatestScalacheckVersion = "3.1.0.1"

val circeVersion = "0.13.0"

ThisBuild / scalaVersion     := "2.13.13"
ThisBuild / crossScalaVersions += "2.12.19"
ThisBuild / organization     := "com.ovoenergy"
ThisBuild / organizationName := "OVO energy"
ThisBuild / dynverSeparator  := "-"


lazy val dataduggee = (project in file("."))
  .settings(
    name := "dataduggee",
    publishTo := Some("Kaluza Artifactory" at "https://kaluza.jfrog.io/artifactory/maven"),
    startYear := Some(2019),
    licenses := Seq(
      "Apache-2.0" -> url("https://opensource.org/licenses/apache-2.0")
    ),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/ovotech/dataduggee"),
        "scm:git:git@github.com:ovotech/dataduggee.git"
      )
    ),
    // TODO Extract contributors from github
    developers := List(
      Developer("ovotech/comms", "OVO Comms team", "hello.comms@ovoenergy.com", url("https://github.com/orgs/ovotech/teams/comms")),
    ),
    credentials += {
      for {
        usr <- sys.env.get("ARTIFACTORY_USER")
        password <- sys.env.get("ARTIFACTORY_PASS")
      } yield Credentials("Artifactory Realm", "kaluza.jfrog.io", usr, password)
    }.getOrElse(Credentials(Path.userHome / ".ivy2" / ".credentials")),
    version ~= (_.replace('+', '-')),
    dynver ~= (_.replace('+', '-')),
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
      "org.http4s" %% "http4s-blaze-core" % http4sBlazeClientVersion,
      "org.http4s" %% "http4s-blaze-client" % http4sBlazeClientVersion,
      "org.scalatest" %% "scalatest" % scalatestVersion % Test,
      "org.scalacheck" %% "scalacheck" % scalacheckVersion % Test,
      "org.scalatestplus" %% "scalacheck-1-14" % scalatestScalacheckVersion % Test,
      "io.circe" %% "circe-parser" % circeVersion % Test,
      "io.circe" %% "circe-core" % circeVersion % Test,
    )
  )
