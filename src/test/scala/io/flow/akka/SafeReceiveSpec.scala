package io.flow.akka

import akka.actor.{Actor, ActorSystem, Props}
import akka.event.Logging
import akka.testkit.{ImplicitSender, TestKit}
import io.flow.log.RollbarLogger
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration._

class SafeReceiveSpec
  extends TestKit(ActorSystem("SafeReceiveSpec"))
  with ImplicitSender
  with AnyWordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  implicit val mockRollbar: RollbarLogger = RollbarLogger.SimpleLoggergs

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "An actor" must {

    "catch and log exceptions" in {
      system.eventStream.subscribe(testActor, classOf[Logging.Error])

      val a = system.actorOf(Props(new Actor {
        override def receive: Receive = SafeReceive { case "boom" =>
          throw new RuntimeException("BOOM!")
        }
      }))

      a ! "boom"

      expectMsgClass(1.second, classOf[Logging.Error])
    }

    "log unhandled" in {
      system.eventStream.subscribe(testActor, classOf[Logging.Error])

      val a = system.actorOf(Props(new Actor {
        override def receive: Receive = SafeReceive.withLogUnhandled { case "test" =>
          ()
        }
      }))

      a ! "unhandled"

      expectMsgClass(1.second, classOf[Logging.Error])
    }

  }

}
