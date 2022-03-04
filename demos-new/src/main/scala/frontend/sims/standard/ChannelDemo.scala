/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package frontend.sims.standard

import frontend.sims.SensorDefinitions
import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{AggregateProgram, BlockG, Builtins}
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.ScafiSimulationInitializer.RadiusSimulation
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.SimulationInfo
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.reflection.Demo
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration.{ScafiProgramBuilder, ScafiWorldInformation}
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.world.ScafiWorldInitializer.Random
import it.unibo.scafi.simulation.s2.frontend.view.scalaFX.drawer.StandardFXOutput
import it.unibo.scafi.space.graphics2D.BasicShape2D.Circle

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
  * Choose a set of nodes and select it as sensor1,
  * Choose another set of nodes and select it as sensor2.
  * The output of computation is showed with the color chosen
  * for output (standard color : purple).
  * If you select another set of nodes and mark it as sensor3 the
  * network recomputes the channel.
  */
@Demo
class Channel extends AggregateProgram with SensorDefinitions with BlockG {

  def channel1(source: Boolean, target: Boolean, width: Double): Boolean =
    distanceTo(source) + distanceTo(target) <= distanceBetween(source, target) + width

  override def main(): Boolean = branch(sense3){false}{channel1(sense1, sense2, 1)}
}


@Demo
class SelfContainedChannel extends AggregateProgram with SensorDefinitions {
  override def main(): Boolean = branch(sense3){false}{channel(sense1, sense2, 5)}

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

  def dilate(region: Boolean, width: Double): Boolean =
    gradient(region) < width

  def channel(src: Boolean, dest: Boolean, width: Double): Boolean =
    dilate(gradient(src) + gradient(dest) <= distBetween(src,dest), width)
}
