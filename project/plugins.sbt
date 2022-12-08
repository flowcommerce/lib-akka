logLevel := Level.Warn
addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat" % "0.4.1")

resolvers += "Flow Plugins" at "https://flow.jfrog.io/flow/plugins-release/"

addSbtPlugin("io.flow" % "sbt-flow-linter" % "0.0.34")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.2")

