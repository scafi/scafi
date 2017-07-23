package sims

import it.unibo.scafi.simulation.gui.model.implementation.SensorEnum
import it.unibo.scafi.incarnations.BasicSimulationIncarnation._

/**
  * @author Roberto Casadei
  *
  */
trait SensorDefinitions extends StandardSensors { self: AggregateProgram =>
  def sense1 = sense[Boolean](SensorEnum.SENS1.name)
  def sense2 = sense[Boolean](SensorEnum.SENS2.name)
  def sense3 = sense[Boolean](SensorEnum.SENS3.name)
  override def nbrRange() = super.nbrRange() * 100
}
