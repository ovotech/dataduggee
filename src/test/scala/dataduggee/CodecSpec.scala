package com.filippodeluca.dataduggee

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

import io.circe.parser.parse
import fs2._

import model._
import codec._
import arbitraries._
import org.scalacheck.Gen

class CodecSpec extends AnyFlatSpec with Matchers with ScalaCheckDrivenPropertyChecks {

  "encodeMetric" should "produce a valid JSON object" in forAll(genMetric) { metric: Metric =>
    parse(encodeMetric(metric)) shouldBe a[Right[_,_]]
  }

  "encodeMetrics" should "produce a valid JSON object" in forAll(Gen.nonEmptyListOf(genMetric)) { metrics: List[Metric] =>
    parse(encodeMetrics(Stream.emits(metrics)).compile.string) shouldBe a[Right[_,_]]
  }

  "encodeEvent" should "produce a valid JSON object" in forAll { event: Event =>
    parse(encodeEvent(event)) shouldBe a[Right[_, _]]
  }

}
