package sims

import it.unibo.scafi.simulation.gui._
import it.unibo.scafi.simulation.gui.model._
import it.unibo.scafi.simulation.gui.BasicSpatialIncarnation.Builtins._
import it.unibo.scafi.simulation.gui.BasicSpatialIncarnation.ID

//import it.unibo.scafi.simulation.gui.BasicSpatialIncarnation.{AggregateProgram => _, _}

object BasicDemo extends Launcher {
  // Configuring simulation
  Settings.Sim_ProgramClass = "sims.Basic" // starting class, via Reflection
  Settings.ShowConfigPanel = false // show a configuration panel at startup
  Settings.Sim_NbrRadius = 0.15 // neighbourhood radius
  Settings.Sim_NumNodes = 100 // number of nodes
  launch()
}

class Basic extends AggregateProgram {
  override def main() = rep(0)(_ + 1) // the aggregate program to run
}

object ChannelDemo extends Launcher {
  // Configuring simulation
  Settings.Sim_ProgramClass = "sims.Channel" // starting class, via Reflection
  Settings.ShowConfigPanel = false // show a configuration panel at startup
  Settings.Sim_NbrRadius = 0.1 // neighbourhood radius
  Settings.Sim_NumNodes = 200 // number of nodes
  Settings.Led_Activator = (b: Any) => b.asInstanceOf[Boolean]
  Settings.To_String = (b: Any) => ""
  launch()
}

trait SensorDefinitions { self: AggregateProgram =>
  def sense1 = sense[Boolean]("sens1")
  def sense2 = sense[Boolean]("sens2")
  def sense3 = sense[Boolean]("sens3")
  def nbrRange = nbrvar[Double]("nbrRange") * 100
}

trait BlockG { self: AggregateProgram with SensorDefinitions =>

  def G[V: OrderingFoldable](source: Boolean)(field: V)(acc: V => V)(metric: => Double = nbrRange): V =
    rep((Double.MaxValue, field)) { case (d,v) =>
      mux(source) { (0.0, field) } {
        minHoodPlus { (nbr{d} + metric, acc(nbr{v})) }
      }
    }._2

  def distanceTo(source: Boolean): Double =
    G(source)(0.0)(_ + nbrRange)()

  def broadcast[V: OrderingFoldable](source: Boolean, field: V): V =
    G(source)(field)(v=>v)()

  def distanceBetween(source: Boolean, target: Boolean): Double =
    broadcast(source, distanceTo(target))
}

class Channel extends AggregateProgram  with SensorDefinitions with BlockG {

  def channel(source: Boolean, target: Boolean, width: Double): Boolean =
    distanceTo(source) + distanceTo(target) <= distanceBetween(source, target) + width

  override def main() = branch(sense3){false}{channel(sense1, sense2, 1)}
}

object CollectionDemo extends Launcher {
  // Configuring simulation
  Settings.Sim_ProgramClass = "sims.Collection" // starting class, via Reflection
  Settings.ShowConfigPanel = false // show a configuration panel at startup
  Settings.Sim_NbrRadius = 0.15 // neighbourhood radius
  Settings.Sim_NumNodes = 100 // number of nodes
  launch()
}


trait BlockC { self: AggregateProgram with SensorDefinitions =>

  def smaller[V: OrderingFoldable](a: V, b: V):Boolean =
    implicitly[OrderingFoldable[V]].compare(a,b)<0

  def findParent[V:OrderingFoldable](potential: V): ID = {
    mux(smaller(minHood { nbr(potential) }, potential)) {
      minHood { nbr { (potential, mid()) } }._2
    } {
      Int.MaxValue
    }
  }

  def C[V: OrderingFoldable](potential: V, acc: (V, V) => V, local: V, Null: V): V = {
    rep(local) { v =>
      acc(local, foldhood(Null)(acc) {
        mux(nbr(findParent(potential)) == mid()) {
          nbr(v)
        } {
          nbr(Null)
        }
      })
    }
  }
}

class Collection extends AggregateProgram with SensorDefinitions with BlockC with BlockG {

  def summarize(sink: Boolean, acc:(Double,Double)=>Double, local:Double, Null:Double): Double =
    broadcast(sink, C(distanceTo(sink), acc, local, Null))

  override def main() = summarize(sense1, _+_, if (sense2) 1.0 else 0.0, 0.0)
}

class CollectionIds extends AggregateProgram with SensorDefinitions with BlockC with BlockG {

  def summarize[V:OrderingFoldable](sink: Boolean, acc:(V,V)=>V, local:V, Null:V): V =
    C[(Double,V)]((distanceTo(sink),local), (a,b) => (a._1, acc(a._2,b._2)), (0.0,local), (0.0,Null))._2

  implicit val ofset = new BasicSpatialIncarnation.Builtins.OrderingFoldable[Set[ID]] {
    override def top: Set[ID] = Set()

    override def bottom: Set[ID] = Set()

    override def compare(a: Set[ID], b: Set[ID]): Int = a.size.compare(b.size)
  }

  override def main() = summarize[Set[ID]](sense1, (_:Set[ID])++(_:Set[ID]), if(sense2) Set(mid()) else Set(), Set())
}

object TimerDemo extends Launcher {
  // Configuring simulation
  Settings.Sim_ProgramClass = "sims.SimpleTimer" // starting class, via Reflection
  Settings.ShowConfigPanel = false // show a configuration panel at startup
  Settings.Sim_NbrRadius = 0.15 // neighbourhood radius
  Settings.Sim_NumNodes = 100 // number of nodes
  //Settings.Led_Activator = (b: Any) => b.asInstanceOf[Boolean]
  //Settings.To_String = (b: Any) => ""
  launch()
}

trait BlockT { self: AggregateProgram with SensorDefinitions =>

  def implicitMin[V: Numeric](a:V, b:V): V = implicitly[Numeric[V]].min(a,b)

  def implicitMax[V: Numeric](a:V, b:V): V = implicitly[Numeric[V]].max(a,b)

  def T[V: Numeric](initial: V)(floor: V)(decay: V => V): V = {
    rep(initial) { v => implicitMin(initial, implicitMax(floor, decay(v))) }
  }

  def linearFlow(time: Double): Double =
    T(time)(0.0)(v => v-1)

  def timer(time: Double): Boolean =
    linearFlow(time) == 0.0
}

class SimpleTimer extends AggregateProgram with SensorDefinitions with BlockT {

  override def main() = branch(sense1){linearFlow(100)}{0}
}

