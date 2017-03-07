import com.github.dnvriend.sbt.GenericSettings



organization in ThisBuild := "com.github.dnvriend"

version in ThisBuild := "1.0.0-SNAPSHOT"

scalaVersion in ThisBuild := "2.11.8"

lazy val helloworld =
  (project in file("helloworld"))
    .enablePlugins(GenericSettings)

lazy val registerPerson =
  (project in file("register-person"))
  .enablePlugins(GenericSettings)
