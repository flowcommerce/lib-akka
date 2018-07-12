package io.flow.akka.recurring

import akka.actor.{Actor, ActorLogging, ActorRef, Timers}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

/**
  * Common utilities to help with scheduling of actors with intervals coming from configuration file.
  */
trait Scheduler extends Timers {
  self: Actor with ActorLogging =>

  /**
    * Helper to schedule a recurring interval based on a configuration
    * parameter.
    *
    * Example:
    *
    *   conf/base.conf:
    *     io.flow.delta.api.CheckProjects.interval = "300 seconds"
    *     io.flow.delta.api.CheckProjects.initial = "3 seconds"
    *
    *   conf/api/actors/...
    *     scheduleRecurring("io.flow.delta.api.CheckProjects", PeriodicActor.Messages.CheckProjects)
    *     scheduleRecurring("io.flow.delta.api.CheckProjects", PeriodicActor.Messages.CheckProjects, otherActor)
    *
    * @param sc The scheduler configuration parameter containing the interval
    *        duration between runs. You can also optionally add an
    *        initial delay duration parameter to be appended to set the
    *        initial interval if you wish it to be different.
    */
  def scheduleRecurring(sc: ScheduleConfig,
                        msg: Any,
                        receiver: ActorRef = this.self
                       )(implicit ec: ExecutionContext): Unit = {
    log.info(s"[${getClass.getName}] scheduleRecurring[$sc]: Initial[${sc.initial}], Interval[${sc.interval}]")
    context.system.scheduler.schedule(sc.initial.getOrElse(sc.interval), sc.interval, receiver, msg)(ec)
  }

  def scheduleRecurringWithDefault(sc: ScheduleConfig,
                                   defaultInitial: FiniteDuration,
                                   msg: Any,
                                   receiver: ActorRef = this.self
                                  )(implicit ec: ExecutionContext): Unit = {
    val initial = sc.initial.getOrElse(defaultInitial)
    log.info(s"[${getClass.getName}] scheduleRecurringWithDefault[$sc]: Initial[$initial], Interval[${sc.interval}]")
    context.system.scheduler.schedule(initial, sc.interval, receiver, msg)(ec)
  }

}
