package it.unibo.scafi.simulation.gui.model.common.sensor

import it.unibo.scafi.simulation.gui.model.core.Device

/**
  * describe a generic sensor
  */
trait Sensor {
  self : Device =>
  /**
    * the value of the sensor
    */
  type VALUE

  /**
    * get the current value of the device
    * @return the value
    */
  def value : VALUE
}