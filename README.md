[![Build Status](https://travis-ci.org/flowcommerce/lib-akka.svg?branch=primary)](https://travis-ci.com/flowcommerce/lib-akka)

# lib-akka

Library containing useful Akka utilities.

## Publishing a new version

    go run release.go

## Publishing a new snapshot for local development

    edit build.sbt and append -SNAPSHOT to version
    sbt +publishLocal
