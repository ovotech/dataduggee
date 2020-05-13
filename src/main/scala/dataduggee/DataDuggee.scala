/*
 * Copyright 2019 OVO energy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

  def createEvent(event: Event): F[Unit]
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
    val postEventUri = config.endpoint / "api" / "v1" / "events" +? ("api_key", config.apiKey)

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
        response <- client.successful(req).ensure(new Exception("Failed to send metric to DataDog."))(_ == true)
      } yield response

      response.void
    }

    def createEvent(event: Event): F[Unit] = {
      val body = codec.encodeEvent(event)
      val response = for {
        req <- POST(body, postEventUri, `Content-Type`(MediaType.application.json))
        resp <- client.successful(req).ensure(new Exception("Failed to create event in DataDog."))(_ == true)
      } yield resp
      response.void
    }
  }
}
