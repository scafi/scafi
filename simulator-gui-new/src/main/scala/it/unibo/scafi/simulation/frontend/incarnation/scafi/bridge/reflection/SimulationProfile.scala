package it.unibo.scafi.simulation.frontend.incarnation.scafi.bridge.reflection

import it.unibo.scafi.simulation.frontend.configuration.SensorName._
import it.unibo.scafi.simulation.frontend.configuration.command.CommandBinding
import it.unibo.scafi.simulation.frontend.incarnation.scafi.ScafiCommandBinding.{AdHocToggleBinding, StandardBinding}
import it.unibo.scafi.simulation.frontend.incarnation.scafi.bridge.ExportEvaluation.EXPORT_EVALUATION
import it.unibo.scafi.simulation.frontend.incarnation.scafi.bridge.{ExportEvaluation, MetaActionProducer}
import it.unibo.scafi.simulation.frontend.incarnation.scafi.world.{ScafiDeviceProducers, scafiWorld}
import it.unibo.scafi.simulation.frontend.model.sensor.SensorConcept
import it.unibo.scafi.simulation.frontend.view.AbstractKeyboardManager.Code1

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
    * @return a list of actuator uses into scafi world
    */
  def actions : List[MetaActionProducer[_]]

  /**
    * @return a list of export evaluation uses to valutate and produce change in gui world
    */
  def evaluations : List[EXPORT_EVALUATION[_]]
}

object SimulationProfile {

  /**
    * standard scafi profile : there is a set of on off input sensor and a set of any output sensor
    * a possible scenario:
    *   you want create a simulation with tree on off input device, and only a type of output
    */
  object standardProfile extends SimulationProfile {
    override val commandMapping: CommandBinding = StandardBinding

    override val sensorSeed: Iterable[scafiWorld.DEVICE_PRODUCER] = ScafiDeviceProducers.standardConfiguration

    override val actions : List[MetaActionProducer[_]] = List.empty

    override val evaluations : List[EXPORT_EVALUATION[_]] = List(ExportEvaluation.standardEvaluation)
  }

  /**
    * a scafi profile that describe a simulation with one on off sensor and any output sensor
    * a possible scenario:
    *   you want create a simulation with only an on off device with only one type of output
    */
  object onOffInputAnyOutput extends SimulationProfile {
    override val commandMapping: CommandBinding = AdHocToggleBinding(Map(Code1 -> sensor1))

    override val sensorSeed: Iterable[scafiWorld.DEVICE_PRODUCER] = ScafiDeviceProducers.ahHocDeviceConfiguration(List((sensor1,false,SensorConcept.sensorInput),
      (output1,"",SensorConcept.sensorOutput)))

    override val actions : List[MetaActionProducer[_]] = List.empty

    override val evaluations : List[EXPORT_EVALUATION[_]] = List(ExportEvaluation.standardEvaluation)
  }

  /**
    * a scafi profile that describe a simulation with moving nodes
    * a possible scenario:
    *   you want create a simulation to show a set of drone that are moving in the world with some logic
    */
  object movementProfile extends SimulationProfile {
    override def commandMapping: CommandBinding = StandardBinding

    override def sensorSeed: Iterable[scafiWorld.DEVICE_PRODUCER] = ScafiDeviceProducers.standardConfiguration

    override val actions : List[MetaActionProducer[_]] = List(MetaActionProducer.movementDtActionProducer)

    override val evaluations : List[EXPORT_EVALUATION[_]] = List.empty
  }
}