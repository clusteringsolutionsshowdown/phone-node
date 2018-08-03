name := "phone-node"
version := "0.0.1"
scalaVersion := "2.12.6"
organization := "io.ticofab"

lazy val phoneCommon = RootProject(file("../phone-common"))
val main = Project(id = "phone-app", base = file("."))
  .dependsOn(phoneCommon)
