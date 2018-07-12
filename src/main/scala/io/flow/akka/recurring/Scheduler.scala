package io.flow.akka.recurring

import akka.actor.{Actor, ActorLogging, ActorRef, Timers}
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

/**
  * Common utilities to help with scheduling of actors with intervals coming from configuration file.
  */
trait Scheduler extends Timers {
  self: Actor with ActorLogging =>

  def config: Config

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
    * @param configName The name of the configuration parameter containing the number
    *        of seconds between runs. You can also optionally add a
    *        configuration parameter of the same name with "_inital"
    *        appended to set the initial interval if you wish it to be
    *        different.
    */
  def scheduleRecurring(configName: String,
                        msg: Any,
                        receiver: ActorRef = this.self
                       )(implicit ec: ExecutionContext): Unit = {
    val sc = ScheduleConfig.fromConfig(config, configName)
    log.info(s"[${getClass.getName}] scheduleRecurring[$configName]: Initial[${sc.initial}], Interval[${sc.interval}]")
    context.system.scheduler.schedule(sc.initial.getOrElse(sc.interval), sc.interval, receiver, msg)(ec)
  }

  def scheduleRecurringWithDefault(configName: String,
                                   defaultInitial: FiniteDuration,
                                   msg: Any,
                                   receiver: ActorRef = this.self
                                  )(implicit ec: ExecutionContext): Unit = {
    val sc = ScheduleConfig.fromConfig(config, configName)
    val initial = sc.initial.getOrElse(defaultInitial)
    log.info(s"[${getClass.getName}] scheduleRecurringWithDefault[$configName]: Initial[$initial], Interval[${sc.interval}]")
    context.system.scheduler.schedule(initial, sc.interval, receiver, msg)(ec)
  }

}
