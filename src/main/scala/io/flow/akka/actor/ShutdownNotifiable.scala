package io.flow.akka.actor

trait ShutdownNotifiable {
  def shutdownInitiated(): Unit
}
