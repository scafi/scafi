package sims.actor

import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{AggregateProgram, BlockG, Builtins}
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.SimulationInfo
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.actor.ActorPlatformInitializer.RadiusSimulation
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.reflection.Demo
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.{ScafiProgramBuilder, ScafiWorldInformation}
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.ScafiWorldInitializer.Random
import it.unibo.scafi.simulation.gui.view.scalaFX.drawer.StandardFXOutput
import it.unibo.scafi.space.graphics2D.BasicShape2D.Circle
import sims.SensorDefinitions

object ChannelDemo extends App {
  ScafiProgramBuilder (
    Random(500,1920,1080),
    SimulationInfo(program = classOf[Channel]),
    RadiusSimulation(radius = 100),
    neighbourRender = true,
    outputPolicy = StandardFXOutput,
    scafiWorldInfo =
      ScafiWorldInformation(shape = Some(Circle(5)))
  ).launch()
}

/**
  * Channel with obstacles
  *   - Sense1: source area
  *   - Sense2: destination area
  *   - Sense3: obstacles
  */
@Demo
class Channel extends AggregateProgram with SensorDefinitions with BlockG {

  def channel(source: Boolean, target: Boolean, width: Double): Boolean =
    distanceTo(source) + distanceTo(target) <= distanceBetween(source, target) + width

  override def main() = branch(sense3){false}{channel(sense1, sense2, 1)}
}


@Demo
class SelfContainedChannel extends AggregateProgram with SensorDefinitions {
  override def main() = branch(sense3){false}{channel(sense1, sense2, 5)}

  type OB[T] = Builtins.Bounded[T]
  def G[V:OB](src: Boolean, field: V, acc: V=>V, metric: =>Double): V =
    rep( (Double.MaxValue, field) ){ dv =>
      mux(src) { (0.0, field) } {
        minHoodPlus {
          val (d, v) = nbr { (dv._1, dv._2) }
          (d + metric, acc(v))
        } } }._2

  def gradient(source: Boolean): Double =
    G[Double](source, 0, _ + nbrRange(), nbrRange())

  def broadcast[V:OB](source: Boolean, field: V): V =
    G[V](source, field, x=>x, nbrRange())

  def distBetween(source: Boolean, target: Boolean): Double =
    broadcast(source, gradient(target))

  def dilate(region: Boolean, width: Double) =
    gradient(region) < width

  def channel(src: Boolean, dest: Boolean, width: Double) =
    dilate(gradient(src) + gradient(dest) <= distBetween(src,dest), width)
}
