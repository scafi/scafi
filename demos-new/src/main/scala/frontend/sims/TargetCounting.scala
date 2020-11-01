/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package frontend.sims

import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{ScafiStandardAggregateProgram, BlockC, BlockG, BlockS, BlocksWithGC, FieldUtils, ID}
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.ScafiSimulationInitializer.RadiusSimulation
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.SimulationInfo
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.reflection.Demo
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration.ScafiProgramBuilder
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.world.ScafiWorldInitializer.Random

object TargetCounting extends App {
  ScafiProgramBuilder (
    Random(1000,1920,1080),
    SimulationInfo(program = classOf[TargetCountingProgram]),
    RadiusSimulation(radius = 40),
    neighbourRender = true
  ).launch()
}

/**
  * (Incomplete) Draft of target counting as per paper
  *  'Self-stabilising target counting in wireless sensor networks using Euler integration'
  *  http://ieeexplore.ieee.org/abstract/document/8064025/
  */

@Demo
class TargetCountingProgram extends ScafiStandardAggregateProgram with SensorDefinitions
  with FieldUtils with BlockG with BlockS with BlockC with BlocksWithGC {

  def senseTargets: Int = 3

  def neighboursNeighbours(): Map[ID,Set[ID]] =
    includingSelf.mapNbrs(nbr{includingSelf.unionHood(nbr{mid})})

  def chi(nbrNbrs: Map[ID,Set[ID]]): Int = {
    nbrNbrs.keySet.subsets.toList.groupBy(_.size) // E.g., [ 0 -> [{}]
                                                  //          1 -> [{a}, {b}, {c}]        (set of potential 0-simplices)
                                                  //          2 -> [{a,b}, {a,c}, {b,c}]  (set of potential 1-simplices)
                                                  //          3 -> [{a,b,c}]              (set of potential 2-simplices)
                                                  //       ]
      .mapValues(_.filter(set =>
        set.forall(dev => nbrNbrs.get(dev).map(_.intersect(set - dev)==(set-dev)).getOrElse(false))
      ))
      .mapValues(_.size)
      .map(k => (-2 * (k._1 % 2) + 1) * k._2  / (k._1 +1))
      .reduce(_ + _)
  }

  def slices(height: Int): Map[Int,Int] =
    (1 to height).map(_ -> chi(neighboursNeighbours())).toMap

  def localContribute(targets: Int): Int =
    slices(targets).values.fold[Int](0)(_ + _)

  def isLeader: Boolean =
    S(grain = Double.PositiveInfinity, metric = nbr{ () => 1 })

  override def main(): Any =
    summarize(isLeader, _ + _, localContribute(senseTargets), 0)
}