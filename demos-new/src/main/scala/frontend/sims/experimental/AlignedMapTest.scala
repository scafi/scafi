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

package frontend.sims.experimental

import frontend.sims.SensorDefinitions
import it.unibo.scafi.incarnations.BasicSimulationIncarnation._
import it.unibo.scafi.simulation.frontend.incarnation.scafi.bridge.ScafiSimulationInitializer.RadiusSimulation
import it.unibo.scafi.simulation.frontend.incarnation.scafi.bridge.SimulationInfo
import it.unibo.scafi.simulation.frontend.incarnation.scafi.bridge.reflection.Demo
import it.unibo.scafi.simulation.frontend.incarnation.scafi.configuration.ScafiProgramBuilder
import it.unibo.scafi.simulation.frontend.incarnation.scafi.world.ScafiWorldInitializer.Random

object AlignedMapRunner extends App {
  ScafiProgramBuilder (
    Random(50,500,500),
    SimulationInfo(program = classOf[AlignedMapTest]),
    RadiusSimulation(radius = 140),
    neighbourRender = true
  ).launch()
}
@Demo
class AlignedMapTest extends AggregateProgram with SensorDefinitions with FieldUtils with BlockG {
  override def main() = test1

  def test1 = {
    var procs = Map(
      1 -> (()=>f"${distanceTo(sense1)}%.1f"),
      2 -> (()=>f"${distanceTo(sense2)}%.1f"),
      3 -> (()=>f"${distanceTo(sense3)}%.1f"),
      4 -> (()=>f"${distanceTo(sense4)}%.1f"))

    var keyGen = Set[Int]()
    if(sense1) keyGen = keyGen + 1
    if(sense2) keyGen = keyGen + 2
    if(sense3) keyGen = keyGen + 3
    if(sense4) keyGen = keyGen + 4

    var allKeys = rep(keyGen){ myKeys =>
      includingSelf.unionHoodSet(nbr { myKeys ++ keyGen })
    }
    // PROBLEM: how to deal with the removal of processes?

    for(k <- allKeys)
      yield (k -> align(k) { key => procs(key)() })
  }

  def test2 = {
    // Proc descriptor
    case class Proc[T](generator: () => Boolean, comp: (Boolean) => T)

    // Define processes
    val p1 = Proc(() => sense1, (gen) => f"${distanceTo(gen)}%.1f")
    val p2 = Proc(() => sense2, (gen) => f"${-distanceTo(gen)}%.1f")
    val p3 = Proc(() => sense3, (gen) => f"${distanceToWord(gen)}")
    val p4 = Proc(() => sense4, (gen) => f"${distanceTo(gen)}%.1f")

    // Give PIDs to processes
    val procs = Map(1 -> p1, 2 -> p2, 3 -> p3, 4 -> p4)

    alignedMap(procs.keys, (x: Int) => x, (id: Int) => procs(id).comp(procs(id).generator()))
  }

  /**
    * @param t a collection
    * @param f a function mapping t's values to keys
    * @param g a function computing over t's values
    * @return a collection mapping t's values with g by maintaining alignment over the keys
    */
  def alignedMap[A,K,O](t: Traversable[A], f: A => K, g: A => O): Traversable[O] = {
    t.map(a => {
      val k = f(a)
      align(k){ _ => g(a) }
    })
  }

  def distanceToWord(src: Boolean) = {
    val dist = distanceTo(src)
    if(dist < 2) "low"
    else if(dist < 10) "med"
    else "hi"
  }

}
