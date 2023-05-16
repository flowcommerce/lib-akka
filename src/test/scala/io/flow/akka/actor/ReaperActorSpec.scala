package io.flow.akka.actor

import akka.actor.{ActorLogging, ActorSelection, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActors, TestKit, TestProbe}
import io.flow.util.ShutdownNotified
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import scala.concurrent.duration._

import java.util.concurrent.atomic.AtomicLong

object ReaperActorSpec {
  case class SleepFor(millis: Long)

  class SleepyActor(accumulator: AtomicLong) extends ReapedActor with ActorLogging {
    override def receive = {
      case SleepFor(millis) =>
        val cumulativeMillis = accumulator.addAndGet(millis)
        log.info(s"Sleeping for $cumulativeMillis ms")
        Thread.sleep(cumulativeMillis)
    }
  }
}


class ReaperActorSpec extends TestKit(ActorSystem("ReaperActorSpec")) with ImplicitSender
  with AnyWordSpecLike with Matchers with BeforeAndAfterAll {

  import ReaperActorSpec._

  override def beforeAll(): Unit = {
    Reaper.get(system)
    ()
  }

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  private[this] def reaper: ActorSelection =
    system.actorSelection("/user/" + ReaperActor.Name)

  "ReaperActor" must {
    "terminate watched actors" in {
      val reaped = system.actorOf(TestActors.blackholeProps)
      Reaper.get(system).watch(reaped)
      val probe = TestProbe()
      probe.watch(reaped)

      reaper ! ReaperActor.Reap
      probe.expectTerminated(reaped)
      expectMsg(akka.Done) // from reaper when all watched actors have terminated
    }

    "terminate watched notifiables" in {
      var called1 = false
      val notifiable1 = new ShutdownNotified {
        override def shutdownInitiated(): Unit = {
          called1 = true
        }
      }
      Reaper.get(system).watch(notifiable1)

      var called2 = false
      val notifiable2 = new ShutdownNotified {
        override def shutdownInitiated(): Unit = {
          Thread.sleep(3000)
          called2 = true
        }
      }
      Reaper.get(system).watch(notifiable2)

      reaper ! ReaperActor.Reap
      expectMsg(4.seconds, akka.Done) // from reaper when all watched actors have terminated
      called1 mustBe true
      called2 mustBe true
    }
  }

  "allow all messages in watched actors to process" in {
    val accumulator = new AtomicLong(0)
    val reaped = system.actorOf(Props(new SleepyActor(accumulator)))
    Reaper.get(system).watch(reaped)
    val probe = TestProbe()
    probe.watch(reaped)

    val messages = Seq(10L,20,30,40,50,100).map(SleepFor.apply)
    messages.foreach { message =>
      reaped ! message
    }
    reaper ! ReaperActor.Reap
    probe.expectTerminated(reaped)
    expectMsg(akka.Done) // from reaper when all watched actors have terminated
    accumulator.get() mustBe messages.map(_.millis).sum
  }
}
