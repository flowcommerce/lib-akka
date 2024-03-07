package io.flow.akka.actor

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Terminated}

import javax.inject.{Inject, Singleton}
import scala.collection.mutable.{ListBuffer => MutableListBuffer}

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
  private[this] val watchedActors = MutableListBuffer.empty[ActorRef] // Ordering is important to us
  @volatile private[this] var stopSent: Boolean = false

  override def receive: Receive = {
    case ReaperActor.Watch(ref) =>
      if (!watchedActors.contains(ref)) {
        context.watch(ref)
        watchedActors += ref
        log.info(s"Watching actor ${ref.path}")
      }

    case ReaperActor.Reap =>
      if (watchedActors.isEmpty) {
        log.info("All watched actors stopped")
        stopSent = false // for re-use within tests
        sender() ! akka.Done
      } else {
        if (!stopSent) {
          log.info(s"Sending stop to all (${watchedActors.size}) watched actors")
          // Use LIFO in an attempt to unwind how the application initialized
          watchedActors.reverse.foreach { ref =>
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
