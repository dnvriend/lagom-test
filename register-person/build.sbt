
organization in ThisBuild := "com.github.dnvriend"
version in ThisBuild := "1.0.0-SNAPSHOT"
scalaVersion in ThisBuild := "2.11.8"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.2.5" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1" % Test

lazy val person = (project in file("."))
  .enablePlugins(AutomateHeaderPlugin)
  .aggregate(personApi, personImpl)

// lagom uses a multi-module project
// the api project will be used by other projects to
// 'call your services' 
// as Lagom will generate the necessary stubs for you
// when you 'inject' your service
lazy val personApi = (project in file("person-api"))
  .enablePlugins(AutomateHeaderPlugin)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )


lazy val personImpl = (project in file("person-impl"))
  .enablePlugins(AutomateHeaderPlugin)
  .enablePlugins(LagomConductRPlugin)
  .enablePlugins(LagomScala)
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
  .dependsOn(personApi)