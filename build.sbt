name := "lib-akka-akka26"
organization := "io.flow"

scalaVersion := "2.13.3"

javacOptions in doc := Seq("-encoding", "UTF-8")

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

resolvers += "Artifactory" at "https://flow.jfrog.io/flow/libs-release/"

// Must match typesafe play version
//   com.typesafe.akka:akka-actor-typed_2.13:2.6.3
// pulled in by
//   com.typesafe.play:play_2.13:2.8.1
lazy val akkaVersion = "2.6.3"

libraryDependencies ++= Seq(
  "com.iheart" %% "ficus" % "1.5.0",
  "io.flow" %% s"lib-log" % "0.1.37",
  "com.typesafe.akka" %% "akka-actor" % akkaVersion % Provided,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion % Provided,
  "com.typesafe.play" %% "play-json" % "2.9.2" % Provided,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "org.mockito" % "mockito-all" % "1.10.19" % Test,
  "org.scalatest" %% "scalatest" % "3.2.8" % Test,
  "org.scalatest" %% "scalatest-mustmatchers" % "3.2.8" % Test,
  "org.scalatest" %% "scalatest-wordspec" % "3.2.8" % Test,
)

scalacOptions in (Compile, doc) ++= Seq(
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

version := "0.1.47"
