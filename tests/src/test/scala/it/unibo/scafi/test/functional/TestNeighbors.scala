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

import it.unibo.scafi.test.FunctionalTestIncarnation._
import org.scalatest._
import scala.collection.immutable.{Map => IMap}
import scala.collection.mutable.{ArrayBuffer => MArray, Map => MMap}

class TestNeighbors extends FlatSpec with Matchers {
  import ScafiAssertions._
  import ScafiTestUtils._

  private[this] trait SimulationContextFixture {
    var net: Network with SimulatorOps =
      simulatorFactory.gridLike(n = 3, m = 3, stepx = 1, stepy = 1, eps = 0, rng = 1.5)
    implicit val node = new BasicAggregateInterpreter
  }

  it should "be possible to count neighbors" in new SimulationContextFixture {
    // ARRANGE
    import node._
    // ACT
    implicit val endNet = runProgram{ foldhood(0)(_+_){1} } (net)
    // ASSERT
    assertNetworkValues((0 to 8).zip(List(4,6,4,6,9,6,4,6,4)).toMap)
  }

  it should "be possible to count neighbors excluding self" in new SimulationContextFixture {
    // ARRANGE
    import node._
    // ACT
    implicit val endNet = runProgram{
      foldhood(0)(_+_){if (nbr[Int](mid())==mid()) 0 else 1}
    } (net)
    // ASSERT
    assertNetworkValues((0 to 8).zip(List(3,5,3,5,8,5,3,5,3)).toMap)
  }

  it should "be possible for nodes to gather the IDs of their neighbors" in new SimulationContextFixture {
    // ARRANGE
    import node._
    // ACT
    implicit var endNet = runProgram{
      foldhood(List[Int]())(_++_){List(nbr[Int](mid()))}
    } (net)
    // ASSERT
    val values = (0 to 8).zip(List(
      Set(0,1,3,4),Set(0,1,2,4,3,5),Set(1,2,4,5),
      Set(0,1,3,4,6,7),Set(0,1,2,3,4,5,6,7,8),Set(1,2,4,5,7,8),
      Set(3,4,6,7),Set(3,4,5,6,7,8),Set(4,5,7,8)
    )).toMap
    assertNetworkValuesWithPredicate(
      (id: ID, v:List[ID]) => v.sorted == values(id).toList.sorted
    )(true)
  }

  it should "be possible for nodes to gather the IDs of their neighbors excluding themselves" in new SimulationContextFixture {
    // ARRANGE
    import node._
    // ACT
    implicit var endNet = runProgram{
      foldhood(List[Int]())(_++_){
        mux[List[Int]](nbr[Int](mid())==mid()) { List() } { List(nbr[Int](mid())) }
      }
    } (net)
    // ASSERT
    val values = (0 to 8).zip(List(
      Set(1,3,4),    Set(0,2,4,3,5),      Set(1,4,5),
      Set(0,1,4,6,7),Set(0,1,2,3,5,6,7,8),Set(1,2,4,7,8),
      Set(3,4,7),    Set(3,4,5,6,8),      Set(4,5,7)
    )).toMap
    assertNetworkValuesWithPredicate(
      (id: ID, v:List[ID]) => v.sorted == values(id).toList.sorted
    )(passNotComputed = true)
  }

  it should "be possible to calculate the min distance from neighbors, in a grid" in new SimulationContextFixture {
    // ARRANGE
    import node._
    // ACT
    implicit var endNet: Network = runProgram{
      foldhood (Double.MaxValue) ((x,y)=>if (x<y) x else y){
          if (mid()==nbr(mid())) Double.MaxValue else nbrvar[Double]("nbrRange")
        }
    } (net)
    // ASSERT
    assertNetworkValues((0 to 8).zip(List(1,1,1,1,1,1,1,1,1)).toMap)
  }

  it should "be possible to calculate the min distance from neighbors, in a ad-hoc net" in new SimulationContextFixture {
    // ARRANGE
    import node._
    val netToTest = simulatorFactory.basicSimulator(
      idArray = MArray(1,2,3,4),
      nbrMap = MMap(
        1 -> Set(2,3),
        2 -> Set(1,4),
        3 -> Set(1,4),
        4 -> Set(3,2)
      ),
      lsnsMap = MMap(),
      nsnsMap = MMap("nbrRange" -> MMap(
        1 -> MMap(
          2->77, 3->100, 1->0, 4->0
        ),
        2 -> MMap(
          1->77, 4->11, 2->0, 3->0
        ),
        3 -> MMap(
          1->100, 4->55, 2->0, 3->0
        ),
        4 -> MMap(
          3->55, 2->11, 1->0, 4->0
        ))))
    // ACT
    implicit var endNet: Network = runProgram{
      foldhood (Double.MaxValue) ((x,y)=>if (x<y) x else y){
        if (mid()==nbr(mid())) Int.MaxValue else nbrvar[Int]("nbrRange")
      }
    } (netToTest)
    // ASSERT
    assertNetworkValues((1 to 5).zip(List(77,11,55,11)).toMap)
  }
}
