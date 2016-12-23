
organization in ThisBuild := "com.github.dnvriend"
version in ThisBuild := "1.0.0-SNAPSHOT"
scalaVersion in ThisBuild := "2.11.8"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.2.5" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1" % Test

lazy val lagomTest = (project in file("."))
  .aggregate(helloApi, helloService)

lazy val helloApi = (project in file("hello-api"))
  .enablePlugins(AutomateHeaderPlugin)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val helloService = (project in file("hello-service"))
  .enablePlugins(AutomateHeaderPlugin)
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      lagomScaladslPersistenceCassandra,
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(helloApi)


import de.heikoseeberger.sbtheader.license.Apache2_0
licenses +=("Apache-2.0", url("http://opensource.org/licenses/apache2.0.php"))

headers := Map(
  "scala" -> Apache2_0("2016", "Dennis"),
  "conf" -> Apache2_0("2016", "Dennis", "#")
)

import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform

SbtScalariform.autoImport.scalariformPreferences := SbtScalariform.autoImport.scalariformPreferences.value
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
  .setPreference(DoubleIndentClassDeclaration, true)
