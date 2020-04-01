package com.filippodeluca.dataduggee

import cats.implicits._
import fs2._

import model._
import scala.concurrent.duration.FiniteDuration

object codec {

  /**
    * It will generate a payload like this:
    *
    * [[
    * { \"series\" :
    *    [{\"metric\":\"test.metric\",
    *     \"points\":[[timestamp, 20]], 
    *     \"type\":\"rate\",
    *     \"interval\": 20,
    *     \"host\":\"test.example.com\",
    *     \"tags\":[\"environment:test\"]}
    *   ]
    * }
    * ]]
    * 
    * The timestamp must be in POSIX epoch (in seconds)
    */
  def encodeMetric(metric: Metric): String = {

    def encodeMetric(
        name: String,
        points: List[Point],
        typ: String,
        interval: Option[FiniteDuration],
        host: Option[String],
        tags: Set[String]
    ) = {

      def encodePoints(points: List[Point]) = {
        """"points":""" ++ points
          .map { point =>
            s"""[${point.timestamp.getEpochSecond},${point.value}]"""
          }
          .mkString("[", ",", "]")
      }

      def encodeTags(tags: Set[String]) = {
        """"tags":""" ++ tags.map(tencodeTag).mkString("[", ",", "]")
      }

      def encodeTag(tag: Tag): String = {
        tag.value.fold(s""""${tag.name}""""){value => s""""${tag.name}":"${value}""""}
      }

      def encodeHost(host: String) = s""""host":"${host}""""

      def encodeName(name: String) = s""""metric":"${name}""""

      def encodeType(typ: String) = s""""type":"${typ}""""

      def encodeInterval(interval: FiniteDuration) = s""""interval":${interval.toSeconds}"""

      "{" ++ List(
        encodeName(name).some,
        encodePoints(points).some,
        encodeType(typ).some,
        interval.map(encodeInterval),
        host.map(encodeHost),
        encodeTags(tags).some
      ).mapFilter(identity).mkString(",") ++ "}"
    }

    metric match {
      case Count(name, interval, points, host, tags) =>
        encodeMetric(name, points, "count", interval.some, host, tags)
      case Gauge(name, points, host, tags) =>
        encodeMetric(name, points, "gauge", none[FiniteDuration], host, tags)
      case Rate(name, interval, points, host, tags) =>
        encodeMetric(name, points, "rate", interval.some, host, tags)
    }
  }

  def encodeMetrics[F[_]](metrics: Stream[F, Metric]): Stream[F, String] = {
    Stream.emit("""{"series":[""") ++ metrics.map(codec.encodeMetric).intersperse(",") ++ Stream.emit("""]}""")
  }
}
