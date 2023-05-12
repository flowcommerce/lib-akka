package io.flow.akka.actor

trait ShutdownHook {
  def doShutdown(): Unit
}
