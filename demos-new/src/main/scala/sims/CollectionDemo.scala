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

package sims

import it.unibo.scafi.incarnations.BasicSimulationIncarnation._
import Builtins._
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiSimulationInitializer.RadiusSimulation
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.SimulationInfo
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.reflection.Demo
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.ScafiProgramBuilder
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.ScafiWorldInitializer.Random

object CollectionDemo extends App {
  ScafiProgramBuilder (
    Random(50,500,500),
    SimulationInfo(program = classOf[CollectionIds]),
    RadiusSimulation(radius = 140),
    neighbourRender = true
  ).launch()
}

@Demo
class Collection extends AggregateProgram with SensorDefinitions with BlockC with BlockG {

  def summarize(sink: Boolean, acc:(Double,Double)=>Double, local:Double, Null:Double): Double =
    broadcast(sink, C(distanceTo(sink), acc, local, Null))

  override def main() = summarize(sense1, _ + _, if (sense2) 1.0 else 0.0, 0.0)
}
@Demo
class CollectionIds extends AggregateProgram with SensorDefinitions with BlockC with BlockG {

  def summarize[V](sink: Boolean, acc:(V,V)=>V, local:V, Null:V): V =
    C[Double, V](distanceTo3(sink), acc, local, Null)

  import PartialOrderingWithGLB.pogldouble
  def distanceTo3(src: Boolean) = G3[Double](src, 0.0, _ + nbrRange, nbrRange)(pogldouble)

  implicit val ofset = new Builtins.Bounded[Set[ID]] {
    override def top: Set[ID] = Set()

    override def bottom: Set[ID] = Set()

    override def compare(a: Set[ID], b: Set[ID]): Int = a.size.compare(b.size)
  }

  override def main() = summarize[Set[ID]](sense1, (_:Set[ID])++(_:Set[ID]), if(sense2) Set(mid()) else Set(), Set())
}

