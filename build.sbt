import com.github.dnvriend.sbt.GenericSettings

organization in ThisBuild := "com.github.dnvriend"

version in ThisBuild := "1.0.0-SNAPSHOT"

scalaVersion in ThisBuild := "2.11.8"

// cassandra
lagomCassandraEnabled in ThisBuild := true
lagomCassandraCleanOnStart in ThisBuild := true

// kafka
lagomKafkaEnabled in ThisBuild := true
lagomKafkaCleanOnStart in ThisBuild := true

lagomUnmanagedServices in ThisBuild := Map(
  "akka-http-service" -> "http://localhost:18080",
  "akka-http-play-service" -> "http://localhost:18081"
)

val akkaVersion = "2.4.17"

val macwire: ModuleID = "com.softwaremill.macwire" %% "macros" % "2.3.0" % Provided
val scalaTest: ModuleID = "org.scalatest" %% "scalatest" % "3.0.1" % Test
val jwt: ModuleID = "com.jason-goodwin" %% "authentikat-jwt" % "0.4.1"

val kafkaDeps: Seq[ModuleID] = Seq(
  "com.typesafe.akka" %% "akka-stream-kafka" % "0.14",
  "io.confluent" % "kafka-avro-serializer" % "3.2.0",
  "org.apache.kafka" % "kafka-streams" % "0.10.2.0",
  "com.sksamuel.avro4s" %% "avro4s-core" % "1.6.4"
)

val akkaHttpDeps: Seq[ModuleID] = Seq(
  "com.typesafe.akka" %% "akka-http-core" % "10.0.4",
  "com.typesafe.akka" %% "akka-http" % "10.0.4",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.4",
  "com.typesafe.akka" %% "akka-http-jackson" % "10.0.4",
  "com.typesafe.akka" %% "akka-http-xml" % "10.0.4",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.0.4" % Test
)

lazy val `intro-to-lagom` = (project in file("."))
  .aggregate(`hello-api`, `hello-impl`, `person-api`, `person-impl`, `akka-http-service`, `play-service`)

lazy val `hello-api` = (project in file("hello-api"))
  .enablePlugins(AutomateHeaderPlugin)
  .enablePlugins(GenericSettings)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )
  .dependsOn(`serialization-lib`, `auth-lib`)

lazy val `hello-impl` = (project in file("hello-impl"))
  .enablePlugins(AutomateHeaderPlugin)
  .enablePlugins(GenericSettings)
  .enablePlugins(LagomScala)
  //  .enablePlugins(LagomConductRPlugin)
  //  .enablePlugins(Cinnamon)
  .settings(
  libraryDependencies ++= Seq(
    lagomScaladslApi,
    lagomScaladslPersistenceCassandra,
    lagomScaladslKafkaBroker,
    lagomScaladslTestKit,
    macwire,
    scalaTest
  ),
  libraryDependencies ++= kafkaDeps
)
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`hello-api`, `auth-lib`, `cb-lib`, `kafka-lib`)

lazy val `person-api` = (project in file("person-api"))
  .enablePlugins(AutomateHeaderPlugin)
  .enablePlugins(GenericSettings)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `person-impl` = (project in file("person-impl"))
  .enablePlugins(AutomateHeaderPlugin)
  .enablePlugins(GenericSettings)
  .enablePlugins(LagomScala)
  .enablePlugins(LagomConductRPlugin)
  .enablePlugins(Cinnamon)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`person-api`)

lazy val `auth-lib` = (project in file("auth-lib"))
  .enablePlugins(AutomateHeaderPlugin)
  .enablePlugins(GenericSettings)
  .settings(
    libraryDependencies ++= Seq(
      jwt,
      lagomScaladslServer,
      lagomScaladslApi,
      macwire
    ))

lazy val `serialization-lib` = (project in file("serialization-lib"))
  .enablePlugins(AutomateHeaderPlugin)
  .enablePlugins(GenericSettings)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      macwire
    ))

lazy val `cb-lib` = (project in file("cb-lib"))
  .enablePlugins(AutomateHeaderPlugin)
  .enablePlugins(GenericSettings)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      macwire
    ))

lazy val `kafka-lib` = (project in file("kafka-lib"))
  .enablePlugins(AutomateHeaderPlugin)
  .enablePlugins(GenericSettings)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      macwire
    ),
    libraryDependencies ++= kafkaDeps
  )

lazy val `test-lib` = (project in file("test-lib"))
  .enablePlugins(AutomateHeaderPlugin)
  .enablePlugins(GenericSettings)
  .settings(libraryDependencies ++= Seq(
    lagomScaladslApi,
    lagomScaladslTestKit,
    scalaTest,
    macwire
  ))

lazy val `akka-http-service` = (project in file("akka-http-service"))
  .enablePlugins(AutomateHeaderPlugin)
  .enablePlugins(GenericSettings)
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      scalaTest,
      macwire
    ),
    libraryDependencies ++= akkaHttpDeps
  ).dependsOn(`hello-api`)

lazy val `play-service` = (project in file("play-service"))
  .enablePlugins(AutomateHeaderPlugin)
  .enablePlugins(GenericSettings)
  .enablePlugins(LagomPlay && PlayScala)
  .settings(
    libraryDependencies ++= akkaHttpDeps,
    libraryDependencies += macwire,
    libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.2.8",
    libraryDependencies += "com.github.mpilquist" %% "simulacrum" % "0.10.0",
    libraryDependencies += "com.softwaremill.macwire" %% "macros" % "2.3.0" % "provided",
    libraryDependencies += "org.typelevel" %% "scalaz-scalatest" % "1.1.1" % Test,
    libraryDependencies += "org.mockito" % "mockito-core" % "2.6.8" % Test,
    libraryDependencies += "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
    libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
    libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0-M2" % Test
  ).dependsOn(`hello-api`)
