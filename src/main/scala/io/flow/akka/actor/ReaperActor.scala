package io.flow.akka.actor

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Terminated}

import javax.inject.Inject
import scala.collection.mutable.{ListBuffer => MutableListBuffer}

object ReaperActor {
  def name(phase: ManagedShutdownPhase): String = s"flow-reaper-actor-$phase"

  case class Watch(ref: ActorRef)
  case object Reap
}

/** Actor that watches other actors and sends them a PoisonPill when it receives a Reap message. Intended for use in
  * graceful shutdown with CoordinatedShutdown.
  */
private[actor] final class ReaperActor @Inject() (
) extends Actor
  with ActorLogging {
  private[this] val watchedActors = MutableListBuffer.empty[ActorRef] // Ordering is important to us
  private[this] val notifiedActors = MutableListBuffer.empty[ActorRef] // Who we have sent a PoisonPill to

  override def postStop(): Unit = {
    if (watchedActors.isEmpty && notifiedActors.nonEmpty) {
      log.info(s"Successfully reaped all watched actors (${notifiedActors.size})")
    }
    watchedActors.foreach { ref =>
      log.warning(s"Did not receive terminated msg from: ${ref.path.toStringWithoutAddress}")
    }
  }

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
        sender() ! akka.Done
      } else {
        // Use LIFO in an attempt to unwind how the application initialized
        val targets = watchedActors.filterNot(notifiedActors.contains).reverse
        if (targets.nonEmpty) {
          log.info(s"Sending stop to (${targets.size}) watched actors")
          targets.foreach { ref =>
            ref ! PoisonPill // Allow actors to process all messages in mailbox before stopping
            notifiedActors += ref
          }
        }
        self forward ReaperActor.Reap
      }

    case Terminated(ref) =>
      watchedActors -= ref
      log.info(s"Stopped watching ${ref.path}")
  }
}
