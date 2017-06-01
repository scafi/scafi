package lib

import it.unibo.scafi.incarnations.BasicSimulationIncarnation._
import it.unibo.scafi.simulation.gui.model.implementation.SensorEnum

/**
  * @author Roberto Casadei
  *
  */
trait SensorDefinitions { self: AggregateProgram =>
  def sense1 = sense[Boolean](SensorEnum.SENS1.name)
  def sense2 = sense[Boolean](SensorEnum.SENS2.name)
  def sense3 = sense[Boolean](SensorEnum.SENS3.name)
  def nbrRange() = nbrvar[Double](NBR_RANGE_NAME) * 100
}
