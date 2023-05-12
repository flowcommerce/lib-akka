package io.flow.akka.actor

import akka.actor.ActorSystem

trait ShutdownNotified extends ShutdownNotifiable {
  def system: ActorSystem

  Reaper.get(system).watch(this)

}
