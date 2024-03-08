package io.flow.akka.actor

import akka.actor.Actor

/** Mixin to have ActorRef added to set of actors to be terminated early during CoordinatedShutdown. */
trait AsReapedActor {
  this: Actor =>

  def shutdownPhase: ManagedShutdownPhase = ManagedShutdownPhase.ServiceRequestsDone

  Reaper.get(context.system).watch(self, shutdownPhase)
}
