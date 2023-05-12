package io.flow.akka.actor

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Terminated}

import javax.inject.Singleton
import scala.collection.mutable.{Set => MutableSet}

object ReaperActor {
  val Name: String = "flow-reaper-actor"

  case class Watch(ref: ActorRef)
  case class Register(notifiable: ShutdownNotifiable)
  case object Reap
}

/**
 * Actor that watches other actors and sends them a PoisonPill when it receives a Reap message.
 * Intended for use in graceful shutdown with CoordinatedShutdown.
 */
@Singleton
private[actor] final class ReaperActor extends Actor with ActorLogging {
  private[this] val watchedActors = MutableSet.empty[ActorRef]
  private[this] val watchedNotifiables = MutableSet.empty[ShutdownNotifiable]
  @volatile private[this] var stopSent: Boolean = false

  override def receive: Receive = {
    case ReaperActor.Watch(ref) =>
      context.watch(ref)
      watchedActors += ref
      log.info(s"Watching actor ${ref.path}")

    case ReaperActor.Register(hook) =>
      watchedNotifiables += hook
      log.info(s"Watching shutdown hook $hook")

    case ReaperActor.Reap =>
      if (watchedActors.isEmpty && watchedNotifiables.isEmpty) {
        log.info(s"All watched actors and hooks stopped")
        stopSent = false // for re-use within tests
        sender() ! akka.Done
      } else {
        if (!stopSent) {
          log.info(s"Sending stop to all (${watchedActors.size}) watched actors")
          watchedActors.foreach { ref =>
            ref ! PoisonPill // Allow actors to process all messages in mailbox before stopping
          }
          log.info(s"Sending stop to all (${watchedNotifiables.size}) watched shutdown hooks")
          watchedNotifiables.foreach { notifiable =>
            notifiable.shutdownInitiated()
          }
          watchedNotifiables.clear()
          stopSent = true
        }
        self forward ReaperActor.Reap
      }

    case Terminated(ref) =>
      watchedActors -= ref
      log.info(s"Stopped watching ${ref.path}")
  }
}
