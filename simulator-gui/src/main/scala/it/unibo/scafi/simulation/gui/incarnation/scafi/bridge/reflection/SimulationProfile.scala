package it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.reflection

import it.unibo.scafi.simulation.gui.configuration.SensorName._
import it.unibo.scafi.simulation.gui.configuration.command.CommandBinding
import it.unibo.scafi.simulation.gui.incarnation.scafi.ScafiCommandBinding.{AdHocToggleBinding, baseBinding, standardBinding}
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.Actuator
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.Actuator._
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.{ScafiDeviceProducers, scafiWorld}
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
  def sensorSeed : Iterable[scafiWorld.DEVICE_PRODUCER]

  /**
    * @return output action of simulation
    */
  def action : Actuator[_]
}

object SimulationProfile {

  /**
    * standard scafi profile : there is a set of on off input sensor and a set of any output sensor
    */
  object standardProfile extends SimulationProfile {
    override val commandMapping: CommandBinding = standardBinding

    override val sensorSeed: Iterable[scafiWorld.DEVICE_PRODUCER] = ScafiDeviceProducers.standardConfiguration

    override val action: Actuator[_] = generalActuator
  }

  /**
    * a scafi profile that describe a simulation with one on off sensor and any output sensor
    */
  object onOffInputAnyOutput extends SimulationProfile {
    override val commandMapping: CommandBinding = AdHocToggleBinding(Map(Code1 -> sensor1))

    override val sensorSeed: Iterable[scafiWorld.DEVICE_PRODUCER] = ScafiDeviceProducers.ahHocDeviceConfiguration(List((sensor1,false,SensorConcept.sensorInput),
      (output1,"",SensorConcept.sensorOutput)))

    override val action: Actuator[_] = generalActuator
  }

  /**
    * a scafi profile that describe a simulation with moving nodes
    */
  object movementProfile extends SimulationProfile {
    override def commandMapping: CommandBinding = baseBinding

    override def sensorSeed: Iterable[scafiWorld.DEVICE_PRODUCER] = ScafiDeviceProducers.ahHocDeviceConfiguration(List((output1,(0,0),SensorConcept.sensorOutput)))

    override def action: Actuator[_] = movementActuator
  }
}

