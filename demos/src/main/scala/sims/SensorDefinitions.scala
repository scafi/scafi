/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package sims

import it.unibo.scafi.incarnations.BasicSimulationIncarnation._
import it.unibo.scafi.simulation.frontend.model.implementation.SensorEnum

trait SensorDefinitions extends StandardSensors { self: ScafiStandardAggregateProgram =>
  def sense1 = sense[Boolean](SensorEnum.SENS1.name)
  def sense2 = sense[Boolean](SensorEnum.SENS2.name)
  def sense3 = sense[Boolean](SensorEnum.SENS3.name)
  def sense4 = sense[Boolean](SensorEnum.SENS4.name)
  override def nbrRange() = super.nbrRange() * 100
}
