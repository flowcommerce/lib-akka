package io.flow.akka

import FutureActor.Completed
import akka.actor.Stash

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

/**
  * Adds the capability of reacting to messages by running a Future,
  * and waiting for that to finish before processing the next message, without blocking any thread.
  *
  * Note: this uses the `Stash` capabilities of an actor.
  * While a Future is running, all messages will be stashed, and they will be unstashed once it completes.
  *
  * WARNING: the public methods defined here should only be called on an actor's handling thread,
  * so they shouldn't be part of any callbacks on futures. Most of the time they should be used in `receive`'s body.
  *
  * See Scaladoc of `Stash` for information about configuring the stash size.
  * */
trait FutureActor extends Stash {

  /**
    * Stashes messages until the passed future completes.
    * */
  def waitFor[T](f: Future[T]): Unit = {
    waitForAndHandle(f)(_ => ())
  }

  /**
    * Stashes messages until the passed future completes,
    * then runs `handleResult` on the actor's currently assigned thread.
    *
    * After finishing, the actor's behavior should be the one it was using befre calling this function.
    * */
  def waitForAndHandle[T](f: Future[T])(handleResult: Try[T] => Unit): Unit = {
    //only used to send the message to self
    implicit val ec: ExecutionContext = context.dispatcher

    val waiting: Receive = {
      case Completed(result) =>
        unstashAll()
        context.unbecome()

        //trust me, that cast is safe
        //this call is at the end to ensure `handleResult` doesn't change behavior and forget the old one
        handleResult(result.asInstanceOf[Try[T]])

      case _ => stash()
    }

    //shortcut to avoid switching behaviors if the future is already completed
    //in a vast minority of cases
    if(!f.isCompleted) {
      f.onComplete(res => self ! Completed(res))
      context.become(waiting, discardOld = false)
    }
  }
}

private object FutureActor {
  case class Completed[T](result: Try[T])
}
