
organization in ThisBuild := "com.github.dnvriend"
version in ThisBuild := "1.0.0-SNAPSHOT"
scalaVersion in ThisBuild := "2.11.8"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.2.5" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1" % Test

lazy val helloWorld = (project in file("."))
  .enablePlugins(AutomateHeaderPlugin)
  .aggregate(helloWorldApi, helloWorldService)

// lagom uses a multi-module project
// the api project will be used by other projects to
// 'call your services' 
// as Lagom will generate the necessary stubs for you
// when you 'inject' your service
lazy val helloWorldApi = (project in file("hello-api"))
  .enablePlugins(AutomateHeaderPlugin)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

// our implementation. I don't like 'the impl' naming so I called the 
// specification/interface 'api' and the implementation 'service'.
lazy val helloWorldService = (project in file("hello-service"))
  .enablePlugins(AutomateHeaderPlugin)
  .enablePlugins(LagomScala)
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
  .dependsOn(helloWorldApi)