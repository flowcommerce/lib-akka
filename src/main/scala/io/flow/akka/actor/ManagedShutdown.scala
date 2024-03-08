package io.flow.akka.actor

import akka.actor.{ActorSystem, CoordinatedShutdown}
import io.flow.util.{ShutdownNotified, Shutdownable}

sealed trait ManagedShutdownPhase // Subset of service phases listed in CoordinatedShutdown

object ManagedShutdownPhase {
  case object ServiceUnbind extends ManagedShutdownPhase {
    override def toString: String = CoordinatedShutdown.PhaseServiceUnbind
  }
  case object ServiceRequestsDone extends ManagedShutdownPhase {
    override def toString: String = CoordinatedShutdown.PhaseServiceRequestsDone
  }
  case object ServiceStop extends ManagedShutdownPhase {
    override def toString: String = CoordinatedShutdown.PhaseServiceStop
  }

  val All: Seq[ManagedShutdownPhase] = Seq(ServiceUnbind, ServiceRequestsDone, ServiceStop)
}

trait ManagedShutdown extends ShutdownNotified {
  self: Shutdownable =>

  def shutdownPhase: ManagedShutdownPhase = ManagedShutdownPhase.ServiceRequestsDone

  @volatile private var shutdownState: Boolean = false

  def system: ActorSystem

  Reaper.get(system).watch(this, shutdownPhase)

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
