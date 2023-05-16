package io.flow.akka.actor

import akka.actor.Actor
import io.flow.util.ShutdownNotified

class NotifiableReapingActor(notifiable: ShutdownNotified) extends ReapedActor {
  override def receive: Receive = Actor.ignoringBehavior

  override def postStop(): Unit = {
    notifiable.shutdownInitiated()
    super.postStop()
  }
}
