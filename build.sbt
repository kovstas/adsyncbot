lazy val root = (project in file("."))
  .enablePlugins(DockerPlugin, JavaAppPackaging)
  .settings(
    name := "burning_bot",
    organizationName := "dev.kovstas",
    scalaVersion := "2.13.8",
    scalacOptions += "-Ymacro-annotations",
    libraryDependencies ++= {
      val circeVersion = "0.14.2"
      val doobieVersion = "1.0.0-RC2"
      val http4sVersion = "0.23.12"
      val enumeratumVersion = "1.7.0"
      val pureConfigVersion = "0.17.1"

      Seq(
        "org.typelevel" %% "cats-effect" % "3.3.12",
        "org.tpolecat" %% "doobie-core" % doobieVersion,
        "org.tpolecat" %% "doobie-postgres" % doobieVersion,
        "org.tpolecat" %% "doobie-hikari" % doobieVersion,
        "com.github.geirolz" %% "fly4s-core" % "0.0.13",
        "org.http4s" %% "http4s-ember-server" % http4sVersion,
        "org.http4s" %% "http4s-ember-client" % http4sVersion,
        "org.http4s" %% "http4s-dsl" % http4sVersion,
        "org.http4s" %% "http4s-circe" % http4sVersion,
        "io.circe" %% "circe-core" % circeVersion,
        "io.circe" %% "circe-generic" % circeVersion,
        "com.beachape" %% "enumeratum" % enumeratumVersion,
        "io.estatico" %% "newtype" % "0.4.4",
        "org.augustjune" %% "canoe" % "0.6.0",
        "org.typelevel" %% "log4cats-slf4j" % "2.3.0",
        "com.github.pureconfig" %% "pureconfig" % pureConfigVersion,
        "com.github.pureconfig" %% "pureconfig-http4s" % pureConfigVersion,
        "com.github.pureconfig" %% "pureconfig-ip4s" % pureConfigVersion,
        "ch.qos.logback" % "logback-classic" % "1.2.11"
      )
    },
    dockerBaseImage := "openjdk:11-jre",
    dockerUpdateLatest := true,
    dockerExposedPorts ++= Seq(8080),
    dockerBuildOptions ++= Seq("--platform", "linux/amd64"),
    dockerRepository := Some("bregistry.azurecr.io"),
    scalafmtOnCompile := true,
    version ~= (_.replace('+', '-')),
    dynver ~= (_.replace('+', '-')),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
  )
