package io.flow.akka.actor

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Terminated}

import javax.inject.{Inject, Singleton}
import scala.collection.mutable.{Set => MutableSet}

object ReaperActor {
  val Name: String = "flow-reaper-actor"

  case class Watch(ref: ActorRef)
  case object Reap
}

/** Actor that watches other actors and sends them a PoisonPill when it receives a Reap message. Intended for use in
  * graceful shutdown with CoordinatedShutdown.
  */
@Singleton
private[actor] final class ReaperActor @Inject() (
) extends Actor
  with ActorLogging {
  private[this] val watchedActors = MutableSet.empty[ActorRef]
  @volatile private[this] var stopSent: Boolean = false

  override def receive: Receive = {
    case ReaperActor.Watch(ref) =>
      context.watch(ref)
      watchedActors += ref
      log.info(s"Watching actor ${ref.path}")

    case ReaperActor.Reap =>
      if (watchedActors.isEmpty) {
        log.info("All watched actors stopped")
        stopSent = false // for re-use within tests
        sender() ! akka.Done
      } else {
        if (!stopSent) {
          log.info(s"Sending stop to all (${watchedActors.size}) watched actors")
          watchedActors.foreach { ref =>
            ref ! PoisonPill // Allow actors to process all messages in mailbox before stopping
          }
          stopSent = true
        }
        self forward ReaperActor.Reap
      }

    case Terminated(ref) =>
      watchedActors -= ref
      log.info(s"Stopped watching ${ref.path}")
  }
}
