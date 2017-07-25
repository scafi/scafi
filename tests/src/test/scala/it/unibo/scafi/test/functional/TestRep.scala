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

package it.unibo.scafi.test.functional

import it.unibo.scafi.config.GridSettings
import it.unibo.scafi.test.FunctionalTestIncarnation._
import org.scalatest._

import scala.collection.immutable.{Map => IMap}
import scala.collection.mutable.{ArrayBuffer => MArray, Map => MMap}

class TestRep extends FlatSpec with Matchers {
  import ScafiAssertions._
  import ScafiTestUtils._

  val REP = new ItWord

  private[this] trait SimulationContextFixture {
    var net: Network with SimulatorOps =
      simulatorFactory.gridLike(GridSettings(3, 3, 1, 1), rng = 1.5)
    implicit val node = new BasicAggregateInterpreter
  }

  REP should "allow devices to count the number of rounds they execute" in new SimulationContextFixture {
    // ARRANGE
    import node._
    // ACT
    val firingSeq = List(6,0,7,0,8,0,6,8,0,1,1,1,2,2,5,1,0,0,0,0,0)
    implicit val endNet = runProgramInOrder(firingSeq){ rep(0){_+1} } (net)
    // ASSERT
    assertNetworkValues(Map(0 -> 9, 1 -> 4, 2 -> 2, 5 -> 1, 6 -> 2, 7 -> 1, 8 -> 2))
  }

  REP should "work also when nested" in new SimulationContextFixture {
    // ARRANGE
    import node._

    // ACT
    val firingSeq1 = List(0,8,2,2,8,2,2)
    val endNet1 = runProgramInOrder(firingSeq1){
      rep("a"){ _ + "b" + rep(""){ _+"c" } }
    } (net)
    // ASSERT
    assertNetworkValues(Map(0 -> "abc", 8 -> "abcbcc", 2 -> "abcbccbcccbcccc"))(endNet1)

    // ARRANGE
    net.clearExports(); // clear exports from previous run
    val firingSeq2 = List(0,0,2,2,2,2,4,4,4,4,4,4,4,4,4)
    // ACT
    val endNet2 = runProgramInOrder(firingSeq2){
      rep(0){ prev => mux{
        val x = rep(0){_+1}
        x!=0 && (x % 3 == 0 || x % 4 == 0)
      }(prev-1)(prev+1) }
    } (net)
    //ASSERT
    assertNetworkValues(Map(
      0 -> 2,
      2 -> 0,
      4 -> -1
    ))(endNet2)
  }
}
