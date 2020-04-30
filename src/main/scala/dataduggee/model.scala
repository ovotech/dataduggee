package com.filippodeluca.dataduggee

import java.time._
import scala.concurrent.duration.FiniteDuration

object model {

  case class Tag(name: String, value: Option[String])

  case class Point(timestamp: Instant, value: Float)

  sealed trait Metric {
    def host: Option[String]
    def name: String
    def points: List[Point]
    def tags: Set[Tag]
  }

  case class Count(
      name: String,
      interval: FiniteDuration,
      points: List[Point],
      host: Option[String],
      tags: Set[Tag]
  ) extends Metric

  case class Gauge(
      name: String,
      points: List[Point],
      host: Option[String],
      tags: Set[Tag]
  ) extends Metric

  case class Rate(
      name: String,
      interval: FiniteDuration,
      points: List[Point],
      host: Option[String],
      tags: Set[Tag]
  ) extends Metric

  case class Event(
      title: String,
      text: String,
      priority: Option[Event.Priority],
      tags: Set[Tag],
      alertType: Event.AlertType
  )

  object Event {
    sealed trait AlertType { def value: String }
    object AlertType {
      case object Error extends AlertType { val value: String = "error" }
      case object Warning extends AlertType { val value: String = "warning" }
      case object Info extends AlertType { val value: String = "info" }
      case object Success extends AlertType { val value: String = "success" }

      val values: Set[AlertType] = Set(Error, Warning, Info, Success)
    }

    sealed trait Priority { def value: String }
    object Priority {
      case object Normal extends Priority { val value: String = "normal" }
      case object Low extends Priority { val value: String = "low" }

      val values: Set[Priority] = Set(Normal, Low)
    }
  }
}
