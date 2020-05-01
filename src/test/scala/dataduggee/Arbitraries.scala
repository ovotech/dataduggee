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

import java.time.Instant
import scala.concurrent.duration._

import org.scalacheck.Gen
import org.scalacheck.Arbitrary

import model._

object arbitraries {
  import Gen._
  import Arbitrary.arbitrary

  implicit def genToArb[A: Gen]: Arbitrary[A] = Arbitrary(implicitly[Gen[A]])

  def genStringOfSize(
      genSize: Gen[Int] = Gen.choose(0, 24),
      genChars: Gen[Char] = Gen.alphaNumChar
  ): Gen[String] =
    for {
      size <- genSize
      chars <- Gen.listOfN(size, genChars)
    } yield chars.mkString

  val genNonEmptyString: Gen[String] = genStringOfSize(
    genSize = Gen.choose(1, 24)
  )

  val genTag: Gen[Tag] = for {
    n <- choose(0, 3)
    tagName = s"test-dataduggee-tag-$n"
    tagValue <- Gen.option(genNonEmptyString)
  } yield Tag(tagName, tagValue)

  // Generate a instant between now and 30minutes ago
  implicit lazy val genInstant: Gen[Instant] = for {
    now <- Instant.now()
    adj <- choose(0, 30.minutes.toMillis)
  } yield now.minusMillis(adj)

  implicit lazy val genFiniteDuration: Gen[FiniteDuration] = for {
    millis <- choose(0, 60000)
  } yield millis.milliseconds

  implicit lazy val genPoint: Gen[Point] = for {
    timestamp <- arbitrary[Instant]
    value <- arbitrary[Float]
  } yield Point(timestamp, value)

  implicit lazy val genGauge: Gen[Gauge] = for {
    noOfPoints <- chooseNum(1, 100)
    points <- listOfN(noOfPoints, genPoint)
    noOfTags <- chooseNum(0, 3)
    tags <- listOfN(noOfTags, genTag).map(_.toSet)
  } yield Gauge(
    name = "phil.dataduggee.test-gauge",
    points = points,
    host = None,
    tags = tags
  )

  implicit lazy val genCount: Gen[Count] = for {
    noOfPoints <- chooseNum(1, 100)
    points <- listOfN(noOfPoints, genPoint)
    interval <- arbitrary[FiniteDuration]
    noOfTags <- chooseNum(0, 3)
    tags <- listOfN(noOfTags, genTag).map(_.toSet)
  } yield Count(
    name = "phil.dataduggee.test-count",
    points = points,
    interval = interval,
    host = None,
    tags = tags
  )

  implicit lazy val genRate: Gen[Rate] = for {
    noOfPoints <- chooseNum(1, 100)
    points <- listOfN(noOfPoints, genPoint)
    interval <- arbitrary[FiniteDuration]
    noOfTags <- chooseNum(0, 3)
    tags <- listOfN(noOfTags, genTag).map(_.toSet)
  } yield Rate(
    name = "phil.dataduggee.test-rate",
    points = points,
    interval = interval,
    host = None,
    tags = tags
  )

  implicit lazy val genMetric: Gen[Metric] = oneOf(
    genCount,
    genGauge,
    genRate
  )

  implicit lazy val genEventPriority: Gen[Event.Priority] = oneOf(
    Event.Priority.values
  )

  implicit lazy val genEventAlertType: Gen[Event.AlertType] = oneOf(
    Event.AlertType.values
  )

  implicit lazy val genEvent: Gen[Event] = for {
    title <- genNonEmptyString
    text <- genNonEmptyString
    priority <- arbitrary[Option[Event.Priority]]
    alertType <- arbitrary[Event.AlertType]
    noOfTags <- chooseNum(0, 3)
    tags <- listOfN(noOfTags, genTag).map(_.toSet)
  } yield Event(
    title = title,
    text = text,
    priority = priority,
    alertType = alertType,
    tags = tags
  )

}
