package it.unibo.scafi.simulation.gui.demos


import it.unibo.scafi.incarnations.BasicSimulationIncarnation._

trait SensorDefinitions extends StandardSensors { self: AggregateProgram =>
  import it.unibo.scafi.simulation.gui.launcher.scalaFX.WorldConfig._
  def sense1 = sense[Boolean](source.name)
  def sense2 = sense[Boolean](destination.name)
  def sense3 = sense[Boolean](obstacle.name)
  def sense4 = sense[Boolean](gsensor.name)
  override def nbrRange() = super.nbrRange()
}
