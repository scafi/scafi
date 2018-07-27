package it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.reflection

import it.unibo.scafi.simulation.gui.configuration.SensorName._
import it.unibo.scafi.simulation.gui.configuration.command.CommandMapping
import it.unibo.scafi.simulation.gui.incarnation.scafi.ScafiCommandMapping.{AdHocMapping, standardMapping}
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.Actions._
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.ScafiLikeWorld.scafiWorldCommandSpace
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.ScafiSensorSeed
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.ScafiSensorSeed.{AdHocSensorSeed, standardSeed}
import it.unibo.scafi.simulation.gui.model.sensor.SensorConcept
import it.unibo.scafi.simulation.gui.view.AbstractKeyboardManager.Code1

/**
  * describe a simulation profile used with reflection
  */
trait SimulationProfile {
  def commandMapping : CommandMapping

  def sensorSeed : ScafiSensorSeed

  def action : ACTION
}

object SimulationProfile {

  /**
    * standard scafi profile : there is a set of on off input sensor and a set of any output sensor
    */
  object standardProfile extends SimulationProfile {
    override val commandMapping: CommandMapping = standardMapping

    override val sensorSeed: ScafiSensorSeed = standardSeed

    override val action: ACTION = generalAction
  }

  /**
    * a scafi profile that describe a simulation with one on off sensor and any output sensor
    */
  object onOffInputAnyOutput extends SimulationProfile {
    override val commandMapping: CommandMapping = AdHocMapping(Map(Code1 -> ((ids : Set[Any]) => scafiWorldCommandSpace.ToggleDeviceCommand(ids,sensor1))))

    override val sensorSeed: ScafiSensorSeed = AdHocSensorSeed(List((sensor1,false,SensorConcept.sensorInput),
      (output1,"",SensorConcept.sensorOutput)))

    override val action: ACTION = generalAction
  }
}

