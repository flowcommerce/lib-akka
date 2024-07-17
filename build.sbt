name := "lib-akka-akka26"
organization := "io.flow"

scalaVersion := "2.13.14"
ThisBuild / javacOptions ++= Seq("-source", "17", "-target", "17")

enablePlugins(GitVersioning)
git.useGitDescribe := true
coverageDataDir := file("target/scala-2.13")
coverageHighlighting := true
coverageFailOnMinimum := true
coverageMinimumStmtTotal := 70
coverageMinimumBranchTotal := 75

lazy val allScalacOptions = Seq(
  "-feature",
  "-Xfatal-warnings",
  "-unchecked",
  "-Xcheckinit",
  "-Xlint:adapted-args",
  "-Ypatmat-exhaust-depth",
  "100", // Fixes: Exhaustivity analysis reached max recursion depth, not all missing cases are reported.
  "-Wconf:src=generated/.*:silent",
  "-Wconf:src=target/.*:silent", // silence the unused imports errors generated by the Play Routes
  "-release:17",
)

doc / javacOptions := Seq("-encoding", "UTF-8")

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

resolvers += "Artifactory" at "https://flow.jfrog.io/flow/libs-release/"

lazy val akkaVersion = "2.6.20"

libraryDependencies ++= Seq(
  "com.iheart" %% "ficus" % "1.5.2",
  "io.flow" %% "lib-util" % "0.2.46",
  "io.flow" %% s"lib-log" % "0.2.23",
  "com.typesafe.akka" %% "akka-actor" % akkaVersion % Provided,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion % Provided,
  "com.typesafe.play" %% "play-json" % "2.9.4" % Provided,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "org.mockito" % "mockito-all" % "1.10.19" % Test,
  "org.scalatest" %% "scalatest" % "3.2.18" % Test,
  "org.scalatest" %% "scalatest-mustmatchers" % "3.2.19" % Test,
  "org.scalatest" %% "scalatest-wordspec" % "3.2.19" % Test,
)

Test / javaOptions ++= Seq(
  "--add-exports=java.base/sun.security.x509=ALL-UNNAMED",
  "--add-opens=java.base/sun.security.ssl=ALL-UNNAMED",
)
Compile / doc / scalacOptions ++= Seq(
  "-no-link-warnings", // Suppresses problems with Scaladoc links
)

credentials += Credentials(
  "Artifactory Realm",
  "flow.jfrog.io",
  System.getenv("ARTIFACTORY_USERNAME"),
  System.getenv("ARTIFACTORY_PASSWORD"),
)

publishTo := {
  val host = "https://flow.jfrog.io/flow"
  if (isSnapshot.value) {
    Some("Artifactory Realm" at s"$host/libs-snapshot-local;build.timestamp=" + new java.util.Date().getTime)
  } else {
    Some("Artifactory Realm" at s"$host/libs-release-local")
  }
}

scalacOptions ++= allScalacOptions ++ Seq("-release", "17")
scalafmtOnCompile := true
