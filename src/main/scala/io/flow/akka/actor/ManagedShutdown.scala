package io.flow.akka.actor

import akka.actor.ActorSystem
import io.flow.util.{ShutdownNotified, Shutdownable}

trait ManagedShutdown extends ShutdownNotified {
  self: Shutdownable =>

  @volatile private var shutdownState: Boolean = false

  def system: ActorSystem

  Reaper.get(system).watch(this)

  def shutdown(): Unit = {
    shutdownState = true
  }

  override def isShutdown: Boolean = shutdownState

  override def shutdownInitiated(): Unit = {
    if (!shutdownState) {
      shutdown()
      shutdownState = true
    }
  }

}
