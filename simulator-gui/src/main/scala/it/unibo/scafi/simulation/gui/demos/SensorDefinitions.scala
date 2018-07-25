package it.unibo.scafi.simulation.gui.demos


import it.unibo.scafi.incarnations.BasicSimulationIncarnation._

trait SensorDefinitions extends StandardSensors { self: AggregateProgram =>

  import it.unibo.scafi.simulation.gui.configuration.SensorName._
  def sense1 = sense[Boolean](sensor1.name)
  def sense2 = sense[Boolean](sensor2.name)
  def sense3 = sense[Boolean](sensor3.name)
  def sense4 = sense[Boolean](output1.name)
  override def nbrRange() = super.nbrRange()
}

