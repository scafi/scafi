package it.unibo.scafi.simulation.gui.model.implementation

import it.unibo.scafi.simulation.gui.model.Sensor

/**
  * Created by Varini on 14/11/16.
  * Converted/refactored to Scala by Casadei on 3/02/17
  */
object SensorEnum {
  def staticValues = Set(SOURCE, DESTINATION, TEMPERATURE, OBSTACLE)
  val SOURCE = Sensor("Source", false)
  val DESTINATION = Sensor("Destination", false)
  val TEMPERATURE = Sensor ("Temperature", 20.00)
  val OBSTACLE = Sensor ("Obstacle", false)

  var sensors = Set[Sensor]()
  def values = staticValues ++ sensors
}