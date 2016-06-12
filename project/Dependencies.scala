import sbt._

object Dependencies {

  object Versions {
    val scala = "2.11.8"
    val akka = "2.4.3"
    val scalaTest = "2.2.6"
  }

  val repos = Seq(
    "Atlassian Releases" at "https://maven.atlassian.com/public/",
    "JCenter repo" at "https://bintray.com/bintray/jcenter/",
    Resolver.sonatypeRepo("snapshots"),
    "google code" at "http://repo1.maven.org/maven2/com/googlecode/libphonenumber/libphonenumber/"
  )

  val dependencies = Seq(
    "com.typesafe.akka" %% "akka-actor" % Versions.akka,
    "com.typesafe.akka" %% "akka-slf4j" % Versions.akka,
    "com.typesafe.akka" %% "akka-testkit" % Versions.akka % Test,
    "org.scalatest" %% "scalatest" % Versions.scalaTest % Test,
    "com.typesafe.akka" %% "akka-persistence" % Versions.akka,
    "com.github.dnvriend" %% "akka-persistence-inmemory" % "1.2.14",
    "ch.qos.logback" % "logback-classic" % "1.1.2"
  )
}
