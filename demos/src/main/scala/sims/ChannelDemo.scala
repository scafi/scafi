/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package sims

import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{AggregateProgram, BlockG, Builtins}
import it.unibo.scafi.simulation.frontend.{Launcher, Settings}

object ChannelDemo extends Launcher {
  // Configuring simulation
  Settings.Sim_ProgramClass = "sims.SelfContainedChannel" // starting class, via Reflection
  Settings.ShowConfigPanel = false // show a configuration panel at startup
  Settings.Sim_NbrRadius = 0.1 // neighbourhood radius
  Settings.Sim_NumNodes = 200 // number of nodes
  Settings.Led_Activator = (b: Any) => b.asInstanceOf[Boolean]
  Settings.To_String = (b: Any) => ""
  launch()
}

/**
  * Channel with obstacles
  *   - Sense1: source area
  *   - Sense2: destination area
  *   - Sense3: obstacles
  */
class Channel extends AggregateProgram  with SensorDefinitions with BlockG {

  def myChannel(source: Boolean, target: Boolean, width: Double): Boolean =
    distanceTo(source) + distanceTo(target) <= distanceBetween(source, target) + width

  override def main(): Boolean = branch(sense3){false}{myChannel(sense1, sense2, 1)}
}

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
