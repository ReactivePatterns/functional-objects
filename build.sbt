name := "functional-objects"

version := "1.0-SNAPSHOT"
scalaVersion := Dependencies.Versions.scala
scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-unchecked",
  "-language:reflectiveCalls",
  "-language:postfixOps",
  "-language:implicitConversions"
)

libraryDependencies ++= Dependencies.dependencies

resolvers ++= Dependencies.repos
