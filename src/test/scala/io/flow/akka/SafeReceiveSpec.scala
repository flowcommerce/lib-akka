package io.flow.akka

import akka.actor.{Actor, ActorSystem, Props}
import akka.event.Logging
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._

class SafeReceiveSpec extends TestKit(ActorSystem("SafeReceiveSpec")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "An actor" must {

    "catch and log exceptions" in {
      system.eventStream.subscribe(testActor, classOf[Logging.Error])

      val a = system.actorOf(Props(new Actor {
        override def receive: Receive = SafeReceive {
          case "boom" => throw new RuntimeException("BOOM!")
        }
      }))

      a ! "boom"

      expectMsgClass(1.second, classOf[Logging.Error])
    }

    "log unhandled" in {
      system.eventStream.subscribe(testActor, classOf[Logging.Error])

      val a = system.actorOf(Props(new Actor {
        override def receive: Receive = SafeReceive.withLogUnhandled {
          case "test" => ()
        }
      }))

      a ! "unhandled"

      expectMsgClass(1.second, classOf[Logging.Error])
    }

  }

}
