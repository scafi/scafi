/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.simulation.frontend.model.implementation

import java.awt.Color

import it.unibo.scafi.simulation.frontend.Settings
import it.unibo.scafi.simulation.frontend.model.Sensor

import scala.util.{Success, Try}

object SensorEnum {
  val SOURCE: Sensor = Sensor("Source", false)
  val DESTINATION: Sensor = Sensor("Destination", false)
  val TEMPERATURE: Sensor = Sensor("Temperature", 20.00)
  val OBSTACLE: Sensor = Sensor("Obstacle", false)
  val SENS1: Sensor = Sensor("sens1", false)
  val SENS2: Sensor = Sensor("sens2", false)
  val SENS3: Sensor = Sensor("sens3", false)
  val SENS4: Sensor = Sensor("sens4", false)

  //scalastyle:off magic.number
  private val SOURCE_COLOR = new Color(42, 58, 139)
  private val DESTINATION_COLOR = new Color(139, 58, 42)
  private val OBSTACLE_COLOR = new Color(58, 139, 42)
  var sensors: Set[Sensor] = Set(SOURCE, DESTINATION, TEMPERATURE, OBSTACLE, SENS1, SENS2, SENS3, SENS4)

  // scalastyle:off magic.number
  /**
   * Gets the sensor that corresponds to the provided integer.
   * @param sensorIndex the integer value to use
   * @return the sensor related to the integer value, None if it could not be found
   * */
  def fromInt(sensorIndex: Int): Option[Sensor] = sensorIndex match {
    case 1 => Option(SensorEnum.SENS1)
    case 2 => Option(SensorEnum.SENS2)
    case 3 => Option(SensorEnum.SENS3)
    case 4 => Option(SensorEnum.SENS4)
    case _ => None
  }

  /**
   * Gets the color that corresponds to the provided integer.
   * @param sensorIndex the integer value to use
   * @return the color related to the integer value, None if it could not be found
   * */
  def getColor(sensorIndex: Int): Option[Color] = sensorIndex match {
    case 1 => Option(Settings.Color_device1)
    case 2 => Option(Settings.Color_device2)
    case 3 => Option(Settings.Color_device3)
    case 4 => Option(Settings.Color_device4)
    case _ => None
  }

  /**
   * Gets the color that corresponds to the provided sensor.
   * @param sensor the provided sensor
   * @return the color related to the sensor, None if it could not be found
   * */
  def getColor(sensor: Sensor): Option[Color] = {
    val sensorIndex = Try(sensor.name.replace("sens", "").toInt)
    sensorIndex match {
      case Success(index) => getColor(index)
      case _ => sensor match {
        case SOURCE => Option(SOURCE_COLOR)
        case DESTINATION => Option(DESTINATION_COLOR)
        case OBSTACLE => Option(OBSTACLE_COLOR)
        case _ => None
      }
    }
  }
}
