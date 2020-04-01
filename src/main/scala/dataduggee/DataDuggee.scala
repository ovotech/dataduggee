package com.filippodeluca.dataduggee

import scala.concurrent.duration._

import cats.data.NonEmptyList
import cats.implicits._
import cats.effect._
import fs2._

import model._

import org.http4s.Method._
import org.http4s.client._
import org.http4s.syntax.all._
import org.http4s.headers._

import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.Uri
import org.http4s.MediaType
import org.http4s.client.blaze.BlazeClientBuilder
import scala.concurrent.ExecutionContext

trait DataDuggee[F[_]] {
  def sendMetrics(metrics: NonEmptyList[Metric]): F[Unit]
  def pipeMetrics(
    maxMetrics: Int = 1024,
    maxDelay: FiniteDuration = 10.seconds,
    maxConcurrrency: Int = 512
  ): Pipe[F, Metric, Unit]
}

object DataDuggee {

  case class Config(
      apiKey: String,
      endpoint: Uri = uri"https://api.datadoghq.com"
  )

  def resource[F[_]: ConcurrentEffect: Timer](
      config: Config,
      ec: ExecutionContext = ExecutionContext.global
  ): Resource[F, DataDuggee[F]] = {
    BlazeClientBuilder[F](ec).resource.map(client => apply(client, config))
  }
  def apply[F[_]: Concurrent: Timer](client: Client[F], config: Config) = new DataDuggee[F] with Http4sClientDsl[F] {

    val postMetricsUri = config.endpoint / "api" / "v1" / "series" +? ("api_key", config.apiKey)

    def pipeMetrics(
        maxMetrics: Int = 1024,
        maxDelay: FiniteDuration = 10.seconds,
        maxConcurrrency: Int = 512
    ): Pipe[F, Metric, Unit] = { in =>
      in.groupWithin(maxMetrics, maxDelay)
        .mapAsync(maxConcurrrency) { xs =>
          xs.toNel.fold(().pure[F])(sendMetrics)
        }
    }

    def sendMetrics(metrics: NonEmptyList[Metric]): F[Unit] = {

      val body: Stream[F, String] = codec.encodeMetrics(Stream.emits(metrics.toList))

      val response = for {
        req <- POST(body.through(fs2.text.utf8Encode), postMetricsUri, `Content-Type`(MediaType.application.json))
        response <- client.successful[String](req)
      } yield response

      response.void
    }
  }
}
