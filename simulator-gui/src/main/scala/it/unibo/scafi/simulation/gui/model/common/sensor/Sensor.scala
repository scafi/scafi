package it.unibo.scafi.simulation.gui.model.common.sensor

import it.unibo.scafi.simulation.gui.model.common.device.GraphicsDevice
import it.unibo.scafi.simulation.gui.model.core.Device

/**
  * describe a generic sensor
  */
trait Sensor extends Device{
  /**
    * the value of the sensor
    */
  type VALUE

  def getValue : VALUE
}

/**
  * a sensor with a string value
  */
trait TextSensor extends Sensor {
  override type VALUE = String
}

/**
  * a sensor with an associated shape if it is on or off
  */
trait ActivateSensor extends Sensor with GraphicsDevice {
  override type VALUE = Boolean

  def onShape : SHAPE

  def offShape : SHAPE
}
