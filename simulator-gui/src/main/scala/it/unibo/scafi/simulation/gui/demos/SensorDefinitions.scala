package it.unibo.scafi.simulation.gui.demos


import it.unibo.scafi.incarnations.BasicSimulationIncarnation._

trait SensorDefinitions extends StandardSensors { self: AggregateProgram =>

  import it.unibo.scafi.simulation.gui.launcher.SensorName._
  def sense1 = sense[Boolean](sens1.name)
  def sense2 = sense[Boolean](sens2.name)
  def sense3 = sense[Boolean](sens3.name)
  def sense4 = sense[Boolean](gsensor.name)
  override def nbrRange() = super.nbrRange()
}
