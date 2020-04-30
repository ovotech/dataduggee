package com.filippodeluca.dataduggee

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import cats.effect._
import model._
import arbitraries._
import scala.concurrent.ExecutionContext
import cats.data.NonEmptyList

class DataDuggeeSpec extends AnyFlatSpec with Matchers with ScalaCheckDrivenPropertyChecks {

  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val timer: Timer[IO] = IO.timer(ec)
  implicit val cs: ContextShift[IO] = IO.contextShift(ec)

  val config = DataDuggee.Config(
    apiKey = sys.env("DATADOG_API_KEY")
  )

  "DataDuggee" should "submit a gauge to DataDog" in forAll { gauge: Gauge =>
    DataDuggee.resource[IO](config).use { dd =>
      dd.sendMetrics(NonEmptyList.of(gauge))
    }.attempt.unsafeRunSync() shouldBe a[Right[_,_]]
  }

  "DataDuggee" should "submit a count to DataDog" in forAll { count: Count =>
    DataDuggee.resource[IO](config).use { dd =>
      dd.sendMetrics(NonEmptyList.of(count))
    }.attempt.unsafeRunSync() shouldBe a[Right[_,_]]
  }

  "DataDuggee" should "submit a rate to DataDog" in forAll { rate: Rate =>
    DataDuggee.resource[IO](config).use { dd =>
      dd.sendMetrics(NonEmptyList.of(rate))
    }.attempt.unsafeRunSync() shouldBe a[Right[_,_]]
  }

  "DataDuggee" should "create an event in DataDog" in forAll { event: Event =>
    DataDuggee.resource[IO](config).use { dd =>
      dd.createEvent(event)
    }.attempt.unsafeRunSync() shouldBe a[Right[_,_]]
  }

}
