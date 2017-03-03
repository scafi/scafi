package lib

import it.unibo.scafi.incarnations.BasicSimulationIncarnation._

/**
  * @author Roberto Casadei
  *
  */
trait BlockT2 { self: AggregateProgram with SensorDefinitions =>

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
