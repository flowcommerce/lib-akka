package io.flow.akka

import akka.actor.Actor.Receive
import akka.actor.ActorContext
import akka.event.{LogSource, Logging}
import akka.event.Logging.{Error, LogLevel}
import io.flow.log.RollbarLogger

import scala.util.Try
import scala.util.control.NonFatal

/** Wraps an [[akka.actor.Actor.Receive]] block that will log exceptions and unhandled messages.
  *
  * Example usage:
  *
  * class ExampleActor extends Actor with ActorLogging {
  *
  * def receive = io.flow.akka.SafeReceive {
  *
  * case m: ExampleActor.Messages.ExampleMessage => ...
  *
  * case m: Any => ...
  *
  * }
  */
object SafeReceive {
  def apply(r: Receive)(implicit context: ActorContext, logger: RollbarLogger): Receive = {
    new SafeReceive(r, Logging.ErrorLevel, logUnhandled = false)
  }

  def withLogUnhandled(r: Receive)(implicit context: ActorContext, logger: RollbarLogger): Receive = {
    new SafeReceive(r, Logging.ErrorLevel, logUnhandled = true)
  }
}

class SafeReceive(r: Receive, logLevel: LogLevel, logUnhandled: Boolean)(implicit
  context: ActorContext,
  rollbar: RollbarLogger
) extends Receive {

  override def isDefinedAt(v: Any): Boolean = {
    val defined = r.isDefinedAt(v)
    if (!defined && logUnhandled) {
      log("[SafeReceive] Received unhandled message", v) // Logs messages unhandled by the actor here
    }
    defined
  }

  override def apply(v: Any): Unit = Try(r(v)).recover { case NonFatal(e) =>
    log("[SafeReceive] Exception in message handler", v, Some(e))
  }.get

  private def log(errorMsg: String, actorMsg: => Any, exception: Option[Throwable] = None): Unit = {
    if (context.system.eventStream.logLevel >= logLevel) {
      val (str, clazz) = LogSource.fromAnyRef(context.self)
      val msg = s"$errorMsg: $actorMsg"
      val event = exception.fold(Error(str, clazz, msg))(Error(_, str, clazz, msg))
      context.system.eventStream.publish(event)
    }
    val l = rollbar.withKeyValue("actor_message", actorMsg.toString)
    (logLevel, exception) match {
      case (Logging.ErrorLevel, Some(ex)) => l.error(errorMsg, ex)
      case (Logging.ErrorLevel, None) => l.error(errorMsg)
      case (Logging.WarningLevel, Some(ex)) => l.warn(errorMsg, ex)
      case (Logging.WarningLevel, None) => l.warn(errorMsg)
      case (Logging.InfoLevel, Some(ex)) => l.info(errorMsg, ex)
      case (Logging.InfoLevel, None) => l.info(errorMsg)
      case (Logging.DebugLevel, Some(ex)) => l.debug(errorMsg, ex)
      case (Logging.DebugLevel, None) => l.debug(errorMsg)
      case (_, Some(ex)) => l.warn(errorMsg, ex)
      case (_, None) => l.warn(errorMsg)
    }

  }

}
