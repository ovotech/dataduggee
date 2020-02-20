package com.filippodeluca.dataduggee

import scala.concurrent.duration._

import cats.data.NonEmptyList
import cats.implicits._
import fs2._

import model._

import org.http4s.Method._
import org.http4s.client._
import org.http4s.syntax.all._
import org.http4s.headers._

import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.Uri
import org.http4s.MediaType
import cats.effect.Timer
import cats.effect.Concurrent

trait DataDuggee[F[_]] {
  def sendMetrics(metrics: NonEmptyList[Metric]): F[Unit]
}

object DataDuggee {

  case class Config(
    apiKey: String,
    endpoint: Uri = uri"https://api.datadoghq.com",
  )

  def apply[F[_]: Concurrent: Timer](client: Client[F], config: Config) = new DataDuggee[F] with Http4sClientDsl[F] {

    val postMetricsUri = config.endpoint / "api" / "v1" / "series" +? ("api_key", config.apiKey)

    def pipeMetrics(maxMetrics: Int = 1024, maxDelay: FiniteDuration = 10.seconds, maxConcurrrency: Int = 512): Pipe[F, Metric, Unit] = { in => 
      in
        .groupWithin(maxMetrics, maxDelay)
        .mapAsync(maxConcurrrency){ xs =>
          xs.toNel.fold(().pure[F])(sendMetrics) 
        }
    }

    def sendMetrics(metrics: NonEmptyList[Metric]): F[Unit] = {

      val body: Stream[F, String] = Stream.emit("""{"series":[""") ++ Stream
        .emits(metrics.toList)
        .map(codec.encodeMetric)
        Stream.emit("""]}""")

      val response = for { 
        req <- POST(body.through(fs2.text.utf8Encode), postMetricsUri, `Content-Type`(MediaType.application.json))
        response <- client.expect[String](req)
      } yield response

      response.flatMap { str =>
        val normalized = str.replaceAllLiterally(" ", "").replaceAllLiterally("\n","")

        if(normalized == """{"status": "ok"}""") 
          ().pure[F]
        else 
          new Exception(s"Error from Datadog: ${normalized}").raiseError[F, Unit]
      }
    }
  }
}
