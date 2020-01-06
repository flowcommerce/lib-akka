logLevel := Level.Warn
addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat" % "0.1.10")

resolvers += "Flow Plugins" at "https://flow.jfrog.io/flow/plugins-release/"
addSbtPlugin("io.flow" % "sbt-flow-linter" % "0.0.6")
