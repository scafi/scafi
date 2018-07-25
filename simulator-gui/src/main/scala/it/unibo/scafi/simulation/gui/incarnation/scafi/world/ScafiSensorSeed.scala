package it.unibo.scafi.simulation.gui.incarnation.scafi.world

import it.unibo.scafi.simulation.gui.configuration.SensorName._
import it.unibo.scafi.simulation.gui.model.sensor.SensorConcept.{sensorInput, sensorOutput}
/**
  * a set of seed used to create a set of sensor
  */
trait ScafiSensorSeed {
  /**
    * @return a sequence of scafi builder
    */
  def sensor : Iterable[scafiWorld.DEVICE_PRODUCER]
}

object ScafiSensorSeed {

  /**
    * set of sensor seed factory
    */
  case object standard extends ScafiSensorSeed {
    /**
      * @return a sequence of scafi builder
      */
    override lazy val sensor: Iterable[scafiWorld.DEVICE_PRODUCER] = List(scafiWorld.LedProducer(sensor1.name,false,sensorInput),
                                                                                scafiWorld.LedProducer(sensor2.name,false,sensorInput),
                                                                                scafiWorld.LedProducer(sensor3.name,false,sensorInput),
                                                                                scafiWorld.GeneralSensorProducer(name = output1.name,stream = sensorOutput))
  }
}