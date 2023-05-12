package io.flow.akka.actor

import akka.actor.ActorSystem
import io.flow.util.{ShutdownNotifiable, Shutdownable}

trait ShutdownNotified extends Shutdownable with ShutdownNotifiable {
  @volatile private var shutdown: Boolean = false
  def system: ActorSystem

  Reaper.get(system).watch(this)

  override def isShutdown: Boolean = shutdown

  override def shutdownInitiated(): Unit = {
    shutdown = true
  }

}
