/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package sims

import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{ScafiStandardAggregateProgram, ScafiStandardLibraries, ID}
import ScafiStandardLibraries._
import it.unibo.scafi.simulation.frontend.{Launcher, Settings}

object TargetCounting extends Launcher {
  // Configuring simulation
  Settings.Sim_ProgramClass = "sims.TargetCountingProgram" // starting class, via Reflection
  Settings.ShowConfigPanel = false // show a configuration panel at startup
  Settings.Sim_NbrRadius = 0.2 // neighbourhood radius
  Settings.Sim_NumNodes = 50 // number of nodes
  launch()
}
/*
FIX AFTER MERGE
/**
  * (Incomplete) Draft of target counting as per paper
  *  'Self-stabilising target counting in wireless sensor networks using Euler integration'
  *  http://ieeexplore.ieee.org/abstract/document/8064025/
  */
class TargetCountingProgram extends AggregateProgram with SensorDefinitions
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
    S(grain = Double.PositiveInfinity, metric = () => nbr{ 1 })

  override def main(): Any =
    summarize(isLeader, _ + _, localContribute(senseTargets), 0)
}
*/