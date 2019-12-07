/*
 * Copyright (C) 2016-2017, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENCE.txt file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package it.unibo.scafi.simulation.gui.model.implementation

import java.awt.Color

import it.unibo.scafi.simulation.gui.Settings
import it.unibo.scafi.simulation.gui.model.Sensor

import scala.util.{Success, Try}

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

  // scalastyle:off magic.number
  def fromInt(value: Int): Option[Sensor] = value match {
    case 1 => Option(SensorEnum.SENS1)
    case 2 => Option(SensorEnum.SENS2)
    case 3 => Option(SensorEnum.SENS3)
    case 4 => Option(SensorEnum.SENS4)
    case _ => None
  }

  def getColor(sensorIndex: Int): Option[Color] = sensorIndex match {
    case 1 => Option(Settings.Color_device1)
    case 2 => Option(Settings.Color_device2)
    case 3 => Option(Settings.Color_device3)
    case 4 => Option(Settings.Color_device4)
    case _ => None
  }

  def getColor(sensor: Sensor): Option[Color] = {
    val sensorIndex = Try(sensor.name.replace("sens", "").toInt)
    sensorIndex match {case Success(index) => getColor(index); case _ => None}
  }
}
