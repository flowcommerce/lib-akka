package io.flow.akka.recurring

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class SchedulerSpec extends TestKit(ActorSystem("SchedulerSpec")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  val recurringConfig: Config = ConfigFactory.parseString("""
    io.flow.test {
      initial = "5 milliseconds"
      interval = "100 milliseconds"
    }
  """)

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "A recurring scheduled actor" must {

    "schedule a recurring message using config" in {
      system.actorOf(Props(new Actor with ActorLogging with Scheduler {

        override val config: Config = recurringConfig

        override def preStart(): Unit = {
          scheduleRecurring("io.flow.test", "tick")
        }

        override def receive: Receive = {
          case "tick" => testActor ! "scheduled"
        }
      }))

      expectMsg(1.second, "scheduled")
    }

  }

}
