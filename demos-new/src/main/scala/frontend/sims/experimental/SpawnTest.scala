/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
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

  override def main(): String = {
    var procs = Map(
      1 -> SpawnDef(1, ()=>f"${distanceTo(sense4)}%.1f", genCondition = () => sense1),
      2 -> SpawnDef(2, ()=>f"${-distanceTo(sense2)}%.1f", genCondition = () => sense2),
      3 -> SpawnDef(3, ()=>f"${distanceTo(sense3)}%.1f", genCondition = () => sense3, limit = 20))

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
