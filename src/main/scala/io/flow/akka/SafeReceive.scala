package io.flow.akka

import akka.actor.Actor.Receive
import akka.actor.ActorContext
import akka.event.{LogSource, Logging}
import akka.event.Logging.{Error, LogLevel}

import scala.util.Try
import scala.util.control.NonFatal

/**
  * Wraps an [[akka.actor.Actor.Receive]] block that will log exceptions and unhandled messages.
  *
  * Example usage:
  *
  * class ExampleActor extends Actor with ActorLogging {
  *
  * def receive = io.flow.akka.SafeReceive {
  *
  *   case m: ExampleActor.Messages.ExampleMessage => ...
  *
  *   case m: Any => ...
  *
  * }
  */
object SafeReceive {
  def apply(r: Receive)(implicit context: ActorContext): Receive = {
    new SafeReceive(r, Logging.ErrorLevel, logUnhandled = false)(context)
  }

  def withLogUnhandled(r: Receive)(implicit context: ActorContext): Receive = {
    new SafeReceive(r, Logging.ErrorLevel, logUnhandled = true)(context)
  }
}

class SafeReceive(r: Receive, logLevel: LogLevel, logUnhandled: Boolean)(implicit context: ActorContext) extends Receive {

  override def isDefinedAt(v: Any): Boolean = {
    val defined = r.isDefinedAt(v)
    if(!defined && logUnhandled) {
      log(s"FlowEventError unhandled message: $v") // Logs messages unhandled by the actor here
    }
    defined
  }

  override def apply(v: Any): Unit = Try(r(v)).recover {
    case NonFatal(e) => log(v, Some(e))
  }.get

  private def log(msg: => Any, exception: Option[Throwable] = None)(implicit context: ActorContext): Unit = {
    if (context.system.eventStream.logLevel >= logLevel) {
      val (str, clazz) = LogSource.fromAnyRef(context.self)
      val event = exception.fold(Error(str, clazz, msg.toString))(Error(_, str, clazz, msg.toString))
      context.system.eventStream.publish(event)
    }
  }

}
