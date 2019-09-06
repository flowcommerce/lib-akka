name := "lib-akka"
organization := "io.flow"

scalaVersion := "2.12.8"

javacOptions in doc := Seq("-encoding", "UTF-8")

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

resolvers += "Artifactory" at "https://flow.jfrog.io/flow/libs-release/"

lazy val akkaVersion = "2.5.25"

val timeLibSuffix = ""

libraryDependencies ++= Seq(
  "com.iheart" %% "ficus" % "1.4.3",
  "io.flow" %% s"lib-log$timeLibSuffix" % "0.0.82",
  "com.typesafe.akka" %% "akka-actor" % akkaVersion % Provided,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion % Provided,
  "com.typesafe.play" %% "play-json" % "2.7.4" % Provided,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "org.mockito" % "mockito-all" % "1.10.19" % Test,
  "org.scalatest" %% "scalatest" % "3.0.8" % Test
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

version := "0.1.13"
