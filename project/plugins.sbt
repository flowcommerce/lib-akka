logLevel := Level.Warn
addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat" % "0.4.1")

resolvers += "Flow Plugins" at "https://flow.jfrog.io/flow/plugins-release/"

addSbtPlugin("io.flow" % "sbt-flow-linter" % "0.0.41")

addSbtPlugin("com.github.sbt" % "sbt-git" % "2.0.1")

