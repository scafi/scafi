package it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.reflection

import it.unibo.scafi.simulation.gui.configuration.SensorName._
import it.unibo.scafi.simulation.gui.configuration.command.CommandBinding
import it.unibo.scafi.simulation.gui.configuration.seed.DeviceConfiguration
import it.unibo.scafi.simulation.gui.incarnation.scafi.ScafiCommandBinding.{AdHocToggleBinding, baseBinding, standardBinding}
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.Actions._
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.ScafiDeviceSeed.{AdHocDeviceConfiguration, standardConfiguration$}
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.scafiWorld
import it.unibo.scafi.simulation.gui.model.sensor.SensorConcept
import it.unibo.scafi.simulation.gui.view.AbstractKeyboardManager.Code1

/**
  * describe a simulation profile used with reflection
  */
trait SimulationProfile {
  /**
    * @return the simulation command mapping
    */
  def commandMapping : CommandBinding

  /**
    * @return a sensor seed used to initialize sensors
    */
  def sensorSeed : DeviceConfiguration[scafiWorld.DEVICE_PRODUCER]

  /**
    * @return output action of simulation
    */
  def action : ACTION
}

object SimulationProfile {

  /**
    * standard scafi profile : there is a set of on off input sensor and a set of any output sensor
    */
  object standardProfile extends SimulationProfile {
    override val commandMapping: CommandBinding = standardBinding

    override val sensorSeed: DeviceConfiguration[scafiWorld.DEVICE_PRODUCER] = standardConfiguration$

    override val action: ACTION = generalAction
  }

  /**
    * a scafi profile that describe a simulation with one on off sensor and any output sensor
    */
  object onOffInputAnyOutput extends SimulationProfile {
    override val commandMapping: CommandBinding = AdHocToggleBinding(Map(Code1 -> sensor1))

    override val sensorSeed: DeviceConfiguration[scafiWorld.DEVICE_PRODUCER] = AdHocDeviceConfiguration(List((sensor1,false,SensorConcept.sensorInput),
      (output1,"",SensorConcept.sensorOutput)))

    override val action: ACTION = generalAction
  }

  /**
    * a scafi profile that describe a simulation with moving nodes
    */
  object movementProfile extends SimulationProfile {
    override def commandMapping: CommandBinding = baseBinding

    override def sensorSeed: DeviceConfiguration[scafiWorld.DEVICE_PRODUCER] = AdHocDeviceConfiguration(List((output1,(0,0),SensorConcept.sensorOutput)))

    override def action: ACTION = movementAction
  }
}

