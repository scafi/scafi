package sims

import it.unibo.scafi.incarnations.BasicSimulationIncarnation._
import Builtins._
import it.unibo.scafi.simulation.gui.{Launcher, Settings, SettingsSpace}
import lib.{BlockC, BlockG, SensorDefinitions}

/**
  * @author Roberto Casadei
  *
  */
object CollectionDemo extends Launcher {
  // Configuring simulation
  Settings.Sim_ProgramClass = "sims.Collection" // starting class, via Reflection
  Settings.ShowConfigPanel = false // show a configuration panel at startup
  Settings.Sim_NbrRadius = 0.15 // neighbourhood radius
  Settings.Sim_NumNodes = 100 // number of nodes
  launch()
}

class Collection extends AggregateProgram with SensorDefinitions with BlockC with BlockG {

  def summarize(sink: Boolean, acc:(Double,Double)=>Double, local:Double, Null:Double): Double =
    broadcast(sink, C(distanceTo(sink), acc, local, Null))

  override def main() = summarize(sense1, _+_, if (sense2) 1.0 else 0.0, 0.0)
}

class CExample extends AggregateProgram with SensorDefinitions with BlockC with BlockG {

  def summarize(sink: Boolean, acc:(Double,Double)=>Double, local:Double, Null:Double): Double =
    broadcast(sink, C(distanceTo(sink), acc, local, Null))

  def p = distanceTo(sense1)

  override def main() = (SettingsSpace.ToStrings.Default_Double(p), mid()+"->"+findParent(p), C[Double](p, _+_, 1, 0.0))
}

class CollectionIds extends AggregateProgram with SensorDefinitions with BlockC with BlockG {

  def summarize[V: Bounded](sink: Boolean, acc:(V,V)=>V, local:V, Null:V): V =
    C[(Double,V)]((distanceTo(sink),local), (a,b) => (a._1, acc(a._2,b._2)), (0.0,local), (0.0,Null))._2

  implicit val ofset = new Builtins.Bounded[Set[ID]] {
    override def top: Set[ID] = Set()

    override def bottom: Set[ID] = Set()

    override def compare(a: Set[ID], b: Set[ID]): Int = a.size.compare(b.size)
  }

  override def main() = summarize[Set[ID]](sense1, (_:Set[ID])++(_:Set[ID]), if(sense2) Set(mid()) else Set(), Set())
}