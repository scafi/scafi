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
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.ScafiSimulationInitializer.RadiusSimulation
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.SimulationInfo
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.reflection.Demo
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration.ScafiProgramBuilder
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.world.ScafiWorldInitializer.Random

object SpawnTestRunner extends App {
  ScafiProgramBuilder (
    Random(50,500,500),
    SimulationInfo(program = classOf[SpawnTest]),
    RadiusSimulation(radius = 140),
    neighbourRender = true
  ).launch()
}
@Demo
class SpawnTest extends AggregateProgram with SensorDefinitions with FieldUtils with BlockG {
  case class SpawnDef[T](pid: Int,
                         comp: () => T,
                         genCondition: () => Boolean,
                         limit: Double = Double.PositiveInfinity,
                         metric: () => Double = nbrRange,
                         timeGC: Int = 20)
  case class SpawnData[T](value: Option[T] = None,
                          gen: Boolean = false,
                          counter: Option[Int] = None,
                          distance: Double = Double.PositiveInfinity,
                          staleValue: Int = 0)

  override def main() = {
    var procs = Map(
      1 -> SpawnDef(1, ()=>f"${distanceTo(sense4)}%.1f", genCondition = () => sense1),
      2 -> SpawnDef(2, ()=>f"${-distanceTo(sense2)}%.1f", genCondition = () => sense2),
      3 -> SpawnDef(3, ()=>f"${distanceTo(sense3)}%.1f", genCondition = () => sense3, limit = 20))

    var keyGen = procs.values.filter(_.genCondition()).map(_.pid)

    procs.map { case (pid,proc) => pid -> spawn(proc) }.collect{ case (pid, Some(s)) => (pid,s) }.mkString("; ")
  }

  //def spawn[T](gen: ID => T, isGen: Boolean): Option[T] = {
  def spawn[T](p: SpawnDef[T]): Option[T] = {
    val isGen = p.genCondition()
    def processComputation: Option[T] = align("process_computation"){ _ => Some(p.comp()) }
    align(p.pid){ _ =>  // enters the eval context for process of given pid
      rep(SpawnData[T](gen = isGen)){ data =>
        mux(isGen || data.gen){ // Generator node up to previous round
          SpawnData(value = if(isGen){ processComputation } else { None },
                    gen = isGen,
                    counter = if(isGen) Some(data.counter.map(_ + 1).getOrElse(0)) else None,
                    distance = 0.0)
        }{ // Non-generator node
          minHoodSelector[Double,(Double,Option[Int])]{ nbr(data.distance) }{
            (nbr(data.distance) + nbrRange, nbr(data.counter))
          }.map {
            case (newDist, newCount) if newCount.isDefined && newDist <= p.limit && data.staleValue < p.timeGC =>
              data.copy(value = processComputation,
                        gen = false,
                        counter = newCount,
                        distance = newDist,
                        staleValue = if(newCount == data.counter) data.staleValue + 1 else 0)
            case (newDist, newCount) => SpawnData[T](distance = newDist) // Keep distance but resets other fields
          }.getOrElse { SpawnData[T]() }
        }
      }.value
    }
  }

  import Builtins.Bounded
  def minHoodSelector[T: Bounded, V](toMinimize: => T)(data: => V): Option[V] = {
    val ord = implicitly[Bounded[T]]
    foldhoodPlus[(T,Option[V])]((ord.top, None))( (x,y) => if(ord.compare(x._1,y._1) <= 0) x else y )((toMinimize, Some(data)))._2
  }
}
