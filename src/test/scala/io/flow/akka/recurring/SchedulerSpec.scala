package io.flow.akka.recurring

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class SchedulerSpec extends TestKit(ActorSystem("SchedulerSpec")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll: Unit = {
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

  }

}
