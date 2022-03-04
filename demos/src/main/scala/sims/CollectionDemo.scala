/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package sims

import it.unibo.scafi.incarnations.BasicSimulationIncarnation._
import Builtins._
import it.unibo.scafi.simulation.frontend.{Launcher, Settings}

object CollectionDemo extends Launcher {
  // Configuring simulation
  Settings.Sim_ProgramClass = "sims.CollectAndBranch" // starting class, via Reflection
  Settings.ShowConfigPanel = false // show a configuration panel at startup
  Settings.Sim_NbrRadius = 0.15 // neighbourhood radius
  Settings.Sim_NumNodes = 100 // number of nodes
  launch()
}

/**
  * Collection using an 'information propagation subnetwork'
  * Only devices with sense2 active will
  */
class CollectAndBranch extends AggregateProgram with SensorDefinitions with BlockC with BlockS with BlockG {
  override def main(): (ID, List[ID], List[ID]) = {
    val leader = sense1 // S(10, nbrRange)
    val potential = branch(sense2){ distanceTo(leader) }{ Double.PositiveInfinity }
    val coll = C[Double,Set[ID]](potential, _++_, Set(mid), Set()).toList.sorted
    val bcoll = broadcast(leader, coll)
    (mid, coll, bcoll)
  }

  def broadcastAlong[V](potential: Double, field: V, metric: Metric = nbrRange): V =
    G_along(potential, metric, field, (v: V) => v)

}

class Collection extends AggregateProgram with SensorDefinitions with BlockC with BlockG {

  def summarize(sink: Boolean, acc:(Double,Double)=>Double, local:Double, Null:Double): Double =
    broadcast(sink, C(distanceTo(sink), acc, local, Null))

  override def main(): Double = summarize(sense1, _ + _, if (sense2) 1.0 else 0.0, 0.0)
}

class CExample extends AggregateProgram with SensorDefinitions with BlockC with BlockG {

  def summarize(sink: Boolean, acc:(Double,Double)=>Double, local:Double, Null:Double): Double =
    broadcast(sink, C(distanceTo(sink), acc, local, Null))

  def p: Double = distanceTo(sense1)
  override def main(): String = s"${p}, ${mid()} -> ${findParent(p)}, ${C[Double, Double](p, _ + _, 1, 0.0)}"
}

class CollectionIds extends AggregateProgram with SensorDefinitions with BlockC with BlockG {

  def summarize[V](sink: Boolean, acc:(V,V)=>V, local:V, Null:V): V =
    C[Double, V](distanceTo3(sink), acc, local, Null)

  import PartialOrderingWithGLB.pogldouble
  def distanceTo3(src: Boolean): Double = G3[Double](src, 0.0, _ + nbrRange, nbrRange)(pogldouble)

  def G3[V: PartialOrderingWithGLB](source: Boolean, field: V, acc: V => V, metric: => Double): V =
    rep((Double.MaxValue, field)) { case (dist, value) =>
      mux(source) {
        (0.0, field)
      } {
        import PartialOrderingWithGLB._
        minHoodPlusLoc[(Double,V)]((Double.PositiveInfinity, field)) {
          (nbr { dist } + metric, acc(nbr { value }))
        } (poglbTuple(pogldouble, implicitly[PartialOrderingWithGLB[V]]))
      }
    }._2

  implicit val ofset: Bounded[Set[ID]] = new Builtins.Bounded[Set[ID]] {
    override def top: Set[ID] = Set()

    override def bottom: Set[ID] = Set()

    override def compare(a: Set[ID], b: Set[ID]): Int = a.size.compare(b.size)
  }

  override def main(): Set[ID] = summarize[Set[ID]](sense1, (_:Set[ID])++(_:Set[ID]), if(sense2) Set(mid()) else Set(), Set())
}
