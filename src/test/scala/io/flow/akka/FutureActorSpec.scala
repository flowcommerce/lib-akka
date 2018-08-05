package io.flow.akka

import java.time
import java.time.{Instant, LocalDate, Period}

import akka.actor.{Actor, ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.TestKit
import akka.util.Timeout
import org.scalatest.concurrent.{Eventually, ScalaFutures}
import org.scalatest.{Assertions, AsyncWordSpecLike, MustMatchers, WordSpecLike}

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.concurrent.duration._
import scala.util.Success

class FutureActorSpec extends TestKit(ActorSystem("SafeReceiveSpec")) with AsyncWordSpecLike with MustMatchers {
  //for non-blocking work
  private implicit val ec: ExecutionContext = system.dispatcher
  val singleDelay = 100.millis

  //creates a Future that'll complete with Unit after `duration`, without blocking any thread
  def delay(duration: FiniteDuration): Future[Unit] = {
    val promise = Promise[Unit]()

    system.scheduler.scheduleOnce(duration) {
      promise.complete(Success(()))
    }

    promise.future
  }

  class MyActor(job: () => Future[Unit]) extends Actor with FutureActor {

    override def receive: Receive = {
      case "inc" => waitFor(job())
    }
  }

  "waitFor" when {
    "in normal conditions" should {
      "run futures sequentially" in {
        val n = 10

        var jobStamps: List[Long] = Nil

        def incJob(): Future[Unit] = for {
          _ <- delay(singleDelay)
          _ = jobStamps ::= System.currentTimeMillis()
        } yield ()


        val actor = childActorOf(Props(new MyActor(incJob)))
        (1 to n).foreach(_ => actor ! "inc")

        for {
          //wait for all messages to be processed + some delay for actor talk
          _ <- delay(n * singleDelay + 100.millis)
        } yield {
          //for every pair of stamps, the difference must be >=delay
          val allValid = jobStamps.sliding(2).toList.forall {
            case newer :: older :: Nil =>
              (newer - older) >= singleDelay.toMillis

            case _ =>
              fail("impossible (or n=1)")
          }

          allValid mustBe true
        }
      }
    }

    "the initial behavior isn't the default Receive" should {
      "restore behavior after a Future is done" in {

        class BehaviorChangingActor extends Actor with FutureActor {
          def delayRespond(name: String): Unit = {
            val snd = sender()
            waitForAndHandle(delay(50.millis)) { _ => snd ! name }
          }

          val behavior2: Receive = {
            case "whoami" => delayRespond("behavior2")
          }

          val behavior1: Receive = {
            case "switch" => context.become(behavior2)
            case "whoami" => delayRespond("behavior1")
          }

          override def receive: Receive = {
            case "switch" => context.become(behavior1)
            case "whoami" => delayRespond("receive")
          }
        }

        val actor = childActorOf(Props(new BehaviorChangingActor))
        def whoamiResult(): Future[String] = {
          implicit val timeout: Timeout = Timeout(100.millis)

          (actor ? "whoami").mapTo[String]
        }

        for {
          _ <- Future.unit
          who1 <- whoamiResult()
          _ = actor ! "switch"
          who2 <- whoamiResult()
          _ = actor ! "switch"
          who3 <- whoamiResult()
        } yield {
          who1 mustBe "receive"
          who2 mustBe "behavior1"
          who3 mustBe "behavior2"
        }
      }
    }
  }
}
