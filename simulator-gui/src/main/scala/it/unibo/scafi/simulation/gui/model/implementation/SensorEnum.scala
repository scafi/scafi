package it.unibo.scafi.simulation.gui.model.implementation

import it.unibo.scafi.simulation.gui.model.Sensor

/**
  * Created by Varini on 14/11/16.
  * Converted/refactored to Scala by Casadei on 3/02/17
  */
object SensorEnum {
  val SOURCE = Sensor("Source", false)
  val DESTINATION = Sensor("Destination", false)
  val TEMPERATURE = Sensor("Temperature", 20.00)
  val OBSTACLE = Sensor("Obstacle", false)
  val SENS1 = Sensor("sens1", false)
  val SENS2 = Sensor("sens2", false)
  val SENS3 = Sensor("sens3", false)

  var sensors = Set(SOURCE, DESTINATION, TEMPERATURE, OBSTACLE, SENS1, SENS2, SENS3)
}