import com.github.dnvriend.sbt.GenericSettings

organization in ThisBuild := "com.github.dnvriend"

version in ThisBuild := "1.0.0-SNAPSHOT"

scalaVersion in ThisBuild := "2.11.8"

// cassandra
lagomCassandraEnabled := true
lagomCassandraCleanOnStart := true

// kafka
lagomKafkaEnabled := true
lagomKafkaCleanOnStart := true

val macwire: ModuleID = "com.softwaremill.macwire" %% "macros" % "2.3.0" % Provided
val scalaTest: ModuleID = "org.scalatest" %% "scalatest" % "3.0.1" % Test
val jwt: ModuleID = "com.jason-goodwin" %% "authentikat-jwt" % "0.4.1"

val kafkaDeps: Seq[ModuleID] = Seq(
  "com.typesafe.akka" %% "akka-stream-kafka" % "0.14",
  "io.confluent" % "kafka-avro-serializer" % "3.2.0",
  "org.apache.kafka" % "kafka-streams" % "0.10.2.0",
  "com.sksamuel.avro4s" %% "avro4s-core" % "1.6.4"
)

lazy val `intro-to-lagom` = (project in file("."))
  .aggregate(`hello-api`, `hello-impl`)//, `person-api`, `person-impl`)

lazy val `hello-api` = (project in file("hello-api"))
  .enablePlugins(AutomateHeaderPlugin)
  .enablePlugins(GenericSettings)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )
  .dependsOn(`serialization-lib`)

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
