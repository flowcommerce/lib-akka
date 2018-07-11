package io.flow.akka.recurring

import com.typesafe.config.Config

import scala.concurrent.duration.FiniteDuration

case class ScheduleConfig(interval: FiniteDuration, initial: Option[FiniteDuration])

object ScheduleConfig {
  import net.ceedubs.ficus.Ficus._
  import net.ceedubs.ficus.readers.ArbitraryTypeReader._

  def fromConfig(config: Config, path: String): ScheduleConfig = config.as[ScheduleConfig](path)

}
