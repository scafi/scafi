package it.unibo.scafi.simulation.gui.demo


import it.unibo.scafi.incarnations.BasicSimulationIncarnation._

trait SensorDefinitions extends StandardSensors { self: AggregateProgram =>

  import it.unibo.scafi.simulation.gui.configuration.SensorName._
  def sense1 = sense[Boolean](sensor1)
  def sense2 = sense[Boolean](sensor2)
  def sense3 = sense[Boolean](sensor3)
  def sense4 = sense[Boolean](output1)
  override def nbrRange() = super.nbrRange()
}

