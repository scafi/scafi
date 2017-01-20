package sims

import it.unibo.scafi.simulation.gui.BasicSpatialIncarnation.NBR_RANGE_NAME
import it.unibo.scafi.simulation.gui.model.implementation.SensorEnum
import it.unibo.scafi.simulation.gui.BasicSpatialIncarnation._
import Builtins.OrderingFoldable

/**
  * @author Roberto Casadei
  *
  */

trait AggrProgram extends AggregateProgramSpecification with ExecutionTemplate with Constructs with Builtins

class Mid extends AggrProgram {
  override type MainResult = ID

  override def main(): Int = mid()
}

class CountRounds extends AggrProgram {
  override type MainResult = Int

  override def main(): Int = rep(0)(x => x+1)
}

class NumNbrs extends AggrProgram {
  override type MainResult = Int

  override def main() = foldhood(0)(_+_){ nbr{1} }
}

class NumNbrsExceptMyself extends AggrProgram {
  override type MainResult = Int

  override def main() = foldhood(0)(_+_){ if(nbr{mid()}==mid()) 0 else 1 }
}

class MaxId extends AggrProgram {
  override type MainResult = (Int,Int)

  override def main() = {
    val maxId = foldhood(Int.MinValue)(Math.max(_,_)){ nbr(mid()) }
    (mid(), maxId)
  }
}

class Gradient extends AggrProgram {
  override type MainResult = Double

  def isSource = sense[Boolean](SensorEnum.SOURCE.getName)
  def isObstacle = sense[Boolean](SensorEnum.OBSTACLE.getName)

  //gradiente
  override def main(): Double =
    if (isObstacle) {
      Double.MaxValue
    } else {
      rep(Double.MaxValue) {
        distance => mux(isSource){ 0.0 }{ minHoodPlus { nbr { distance } + nbrvar[Double](NBR_RANGE_NAME) } }
      }
    }
}

class GradientHop extends AggrProgram {
  override type MainResult = Int

  def isSource = sense[Boolean](SensorEnum.SOURCE.getName)

  def G[V: OrderingFoldable](source: Boolean, field: V, acc: V => V, metric: => Double): V =
    rep( (Double.MaxValue, field) ){ dv =>
      mux(source) {
        (0.0, field)
      } {
        minHoodPlus {
          val (d, v) = nbr { (dv._1, dv._2) }
          (d + metric, acc(v)) }
      }}._2

  def hopGradientByG(src: Boolean): Double = G[Double](src, 0, _+1, 1)

  override def main(): Int = hopGradientByG(isSource).toInt
}

class Channel extends AggrProgram {
  override type MainResult = Boolean
  override def main() = channel(isSource,isDest,0) //if(channel(isSource, isDest, 0)) 1 else 0

  def isSource = sense[Boolean](SensorEnum.SOURCE.getName)
  def isDest = sense[Boolean](SensorEnum.OBSTACLE.getName)

  def nbrRange():Double = nbrvar[Double](NBR_RANGE_NAME)

  def G[V: OrderingFoldable](source: Boolean, field: V, acc: V => V, metric: => Double): V =
    rep( (Double.MaxValue, field) ){ dv =>
      mux(source) {
        (0.0, field)
      } {
        minHoodPlus {
          val (d, v) = nbr { (dv._1, dv._2) }
          (d + metric, acc(v)) }
      }}._2

  def distanceTo(source:Boolean): Double =
    G[Double](source,0, _ + nbrRange(), nbrRange())

  def broadcast[V: OrderingFoldable](source:Boolean, field: V):V =
    G[V](source,field, x=>x , nbrRange())

  def distanceBetween(source:Boolean, target:Boolean):Double =
    broadcast(source, distanceTo(target))

  def channel2(source:Boolean, target:Boolean, width:Double): (String,String,String) =
    (distanceTo(source).formatted("%.2f"), distanceTo(target).formatted("%.2f"), distanceBetween(source,target).formatted("%.2f"))

  def channel(source:Boolean, target:Boolean, width:Double): Boolean =
    distanceTo(source) + distanceTo(target) <=
      distanceBetween(source,target) + width
}
