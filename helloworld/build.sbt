
organization in ThisBuild := "com.github.dnvriend"
version in ThisBuild := "1.0.0-SNAPSHOT"
scalaVersion in ThisBuild := "2.11.8"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.0" % Provided
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
  .enablePlugins(LagomConductRPlugin)
  .enablePlugins(LagomScala)
  .enablePlugins(Cinnamon)
  .settings(
    libraryDependencies ++= Seq(
      "com.jason-goodwin" %% "authentikat-jwt" % "0.4.1",
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


// ==================================
// ==== scalariform (Code Formatting)
// ==================================
import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform
import sbt.complete.DefaultParsers

SbtScalariform.autoImport.scalariformPreferences in ThisBuild := SbtScalariform.autoImport.scalariformPreferences.value
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
  .setPreference(DoubleIndentClassDeclaration, true)

// =====================================================
// ==== sbt-header (Headers for source and config files)
// =====================================================

licenses in ThisBuild +=("Apache-2.0", url("http://opensource.org/licenses/apache2.0.php"))

import de.heikoseeberger.sbtheader.license.Apache2_0

headers in ThisBuild := Map(
  "scala" -> Apache2_0("2017", "Dennis Vriend"),
  "conf" -> Apache2_0("2017", "Dennis Vriend", "#")
)