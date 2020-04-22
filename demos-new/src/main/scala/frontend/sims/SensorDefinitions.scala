/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package frontend.sims

import it.unibo.scafi.incarnations.BasicSimulationIncarnation._

trait SensorDefinitions extends StandardSensors { self: AggregateProgram =>
  import it.unibo.scafi.simulation.s2.frontend.configuration.SensorName._
  def sense1 = sense[Boolean](sensor1)
  def sense2 = sense[Boolean](sensor2)
  def sense3 = sense[Boolean](sensor3)
  def sense4 = sense[Boolean](sensor4)
  override def nbrRange() = super.nbrRange()
}

