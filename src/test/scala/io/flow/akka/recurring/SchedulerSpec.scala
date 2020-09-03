package io.flow.akka.recurring

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class SchedulerSpec extends TestKit(ActorSystem("SchedulerSpec")) with ImplicitSender
  with AnyWordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A recurring scheduled actor" must {

    "schedule a recurring message using config" in {
      system.actorOf(Props(new Actor with ActorLogging with Scheduler {

        override def preStart(): Unit = {
          scheduleRecurring(ScheduleConfig(50.millis, Some(100.millis)), "tick")
          ()
        }

        override def receive: Receive = {
          case "tick" => testActor ! "scheduled"
        }
      }))

      expectMsg(1.second, "scheduled")
    }

    "schedule a recurring function" in {
      system.actorOf(Props(new Actor with ActorLogging with Scheduler {

        override def preStart(): Unit = {
          scheduleRecurring(ScheduleConfig(50.millis, Some(100.millis))){ self ! "tick" }
          ()
        }

        override def receive: Receive = {
          case "tick" => testActor ! "scheduled"
        }
      }))

      expectMsg(1.second, "scheduled")
    }

  }

}
