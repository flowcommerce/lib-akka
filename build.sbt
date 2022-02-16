name := "lib-akka-akka26"
organization := "io.flow"

scalaVersion := "2.13.6"

lazy val allScalacOptions = Seq(
  "-feature",
  "-Xfatal-warnings",
  "-unchecked",
  "-Xcheckinit",
  "-Xlint:adapted-args",
  "-Ypatmat-exhaust-depth", "100", // Fixes: Exhaustivity analysis reached max recursion depth, not all missing cases are reported.
  "-Wconf:src=generated/.*:silent",
  "-Wconf:src=target/.*:silent", // silence the unused imports errors generated by the Play Routes
)

javacOptions in doc := Seq("-encoding", "UTF-8")

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

resolvers += "Artifactory" at "https://flow.jfrog.io/flow/libs-release/"

// Must match typesafe play version - https://github.com/playframework/playframework/blob/2.8.11/project/Dependencies.scala#L10
lazy val akkaVersion = "2.6.17"

libraryDependencies ++= Seq(
  "com.iheart" %% "ficus" % "1.5.1",
  "io.flow" %% s"lib-log" % "0.1.64",
  "com.typesafe.akka" %% "akka-actor" % akkaVersion % Provided,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion % Provided,
  "com.typesafe.play" %% "play-json" % "2.9.2" % Provided,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "org.mockito" % "mockito-all" % "1.10.19" % Test,
  "org.scalatest" %% "scalatest" % "3.2.11" % Test,
  "org.scalatest" %% "scalatest-mustmatchers" % "3.2.11" % Test,
  "org.scalatest" %% "scalatest-wordspec" % "3.2.11" % Test,
)

Compile / doc / scalacOptions ++= Seq(
  "-no-link-warnings" // Suppresses problems with Scaladoc links
)

credentials += Credentials(
  "Artifactory Realm",
  "flow.jfrog.io",
  System.getenv("ARTIFACTORY_USERNAME"),
  System.getenv("ARTIFACTORY_PASSWORD")
)

publishTo := {
  val host = "https://flow.jfrog.io/flow"
  if (isSnapshot.value) {
    Some("Artifactory Realm" at s"$host/libs-snapshot-local;build.timestamp=" + new java.util.Date().getTime)
  } else {
    Some("Artifactory Realm" at s"$host/libs-release-local")
  }
}

version := "0.1.70"
