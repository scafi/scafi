/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.simulation.frontend.model.implementation

import it.unibo.scafi.simulation.frontend.model.Sensor

object SensorEnum {
  val SOURCE = Sensor("Source", false)
  val DESTINATION = Sensor("Destination", false)
  val TEMPERATURE = Sensor("Temperature", 20.00)
  val OBSTACLE = Sensor("Obstacle", false)
  val SENS1 = Sensor("sens1", false)
  val SENS2 = Sensor("sens2", false)
  val SENS3 = Sensor("sens3", false)
  val SENS4 = Sensor("sens4", false)

  var sensors = Set(SOURCE, DESTINATION, TEMPERATURE, OBSTACLE, SENS1, SENS2, SENS3, SENS4)
}
