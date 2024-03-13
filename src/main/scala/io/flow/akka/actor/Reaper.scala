package io.flow.akka.actor

import akka.actor.{ActorRef, ActorSystem, Extension, ExtensionId, ExtensionIdProvider, Props}
import akka.pattern.ask
import akka.util.Timeout
import io.flow.util.ShutdownNotified

import scala.concurrent.Future

/** To have an ActorRef terminated early in the CoordinatedShutdown process:
  *
  *   1. Add the ActorRef to the set to be managed {{{Reaper.get(system).watch(actorRef)}}}
  *
  * 2. Call {{{Reaper.get(system).reapAsync()}}} during shutdown to signal for all actors watched by the Reaper to be
  * terminated. This is done automatically by CoordinatedShutdownActorReaperModule if loaded.
  */
object Reaper extends ExtensionId[Reaper] with ExtensionIdProvider {
  override def lookup = Reaper

  override def createExtension(system: akka.actor.ExtendedActorSystem) = new Reaper(system)
}

final class Reaper private[actor] (system: ActorSystem) extends Extension {
  private[this] val reaperUnbind: ActorRef =
    system.actorOf(Props(classOf[ReaperActor]), ReaperActor.name(ManagedShutdownPhase.ServiceUnbind))
  private[this] val reaperRequestsDone: ActorRef =
    system.actorOf(Props(classOf[ReaperActor]), ReaperActor.name(ManagedShutdownPhase.ServiceRequestsDone))
  private[this] val reaperStop: ActorRef =
    system.actorOf(Props(classOf[ReaperActor]), ReaperActor.name(ManagedShutdownPhase.ServiceStop))

  def watch(ref: akka.actor.ActorRef, phase: ManagedShutdownPhase): Unit =
    reaper(phase) ! ReaperActor.Watch(ref)

  def watch(notifiable: ShutdownNotified, phase: ManagedShutdownPhase): Unit = {
    val id = s"$notifiable${notifiable.hashCode()}Wrapper".filter(_.isLetterOrDigit)
    val wrapper = system.actorOf(Props(classOf[NotifiableReapingActor], notifiable), id)
    watch(wrapper, phase)
  }

  def reapAsync(phase: ManagedShutdownPhase)(implicit timeout: Timeout): Future[akka.Done] = {
    (reaper(phase) ? ReaperActor.Reap).mapTo[akka.Done]
  }

  private[this] def reaper(phase: ManagedShutdownPhase): ActorRef = phase match {
    case ManagedShutdownPhase.ServiceUnbind => reaperUnbind
    case ManagedShutdownPhase.ServiceRequestsDone => reaperRequestsDone
    case ManagedShutdownPhase.ServiceStop => reaperStop
  }
}
