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

package com.ovoenergy.dataduggee

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
    parse(encodeMetric(metric)) shouldBe a[Right[_, _]]
  }

  "encodeMetrics" should "produce a valid JSON object" in forAll(Gen.nonEmptyListOf(genMetric)) {
    metrics: List[Metric] =>
      parse(encodeMetrics(Stream.emits(metrics)).compile.string) shouldBe a[Right[_, _]]
  }

  "encodeEvent" should "produce a valid JSON object" in forAll { event: Event =>
    parse(encodeEvent(event)) shouldBe a[Right[_, _]]
  }

}
