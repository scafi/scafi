/*
 * Copyright (C) 2016-2017, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENCE.txt file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package it.unibo.scafi.test.simulator

import it.unibo.scafi.test.FunctionalTestIncarnation._
import org.scalatest.{FunSuite, Matchers}

class TestDeterminism extends FunSuite with Matchers {

  test("Determinism in network creation"){
    /**
      * gridLike uses 'eps' param to determine the extent of random displacements.
      * Here, we verify that a system is configurated at the same way as others when their factories
      * use the same random seed for configuration, and they differ otherwise.
      */
    val net1 = simulatorFactory.gridLike(20, 20, 1, 1, eps = 5, 1.5, seeds = Seeds(configSeed = 5L))
    val net2 = simulatorFactory.gridLike(20, 20, 1, 1, eps = 5, 1.5, seeds = Seeds(configSeed = 5L))
    val net3 = simulatorFactory.gridLike(20, 20, 1, 1, eps = 5, 1.5, seeds = Seeds(configSeed = 10101010L))

    net1.ids.forall(id =>
      net1.neighbourhood(id) == net2.neighbourhood(id)
    ) shouldBe(true)

    net1.ids.exists(id =>
      net1.neighbourhood(id) != net3.neighbourhood(id)
    ) shouldBe(true)
  }

}
