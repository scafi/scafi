package it.unibo.scafi.simulation.gui.incarnation.scafi.world

import it.unibo.scafi.simulation.gui.configuration.SensorName._
import it.unibo.scafi.simulation.gui.configuration.seed.DeviceSeed
import it.unibo.scafi.simulation.gui.model.sensor.SensorConcept.{SensorStream, sensorInput, sensorOutput}

/**
  * describe a set of device seed in scafi context
  */
object ScafiDeviceSeed {

  /**
    * set of sensor seed factory
    */
  case object standardSeed extends DeviceSeed[scafiWorld.DEVICE_PRODUCER] {
    /**
      * @return a sequence of scafi builder
      */
    override lazy val devices: Iterable[scafiWorld.DEVICE_PRODUCER] = List(scafiWorld.LedProducer(sensor1,false,sensorInput),
                                                                                scafiWorld.LedProducer(sensor2,false,sensorInput),
                                                                                scafiWorld.LedProducer(sensor3,false,sensorInput),
                                                                                scafiWorld.GeneralSensorProducer(name = output1,stream = sensorOutput))
  }

  /**
    * a class that describe a set of producer passed
    * @param sens a set of sensor describe with a name, default value ad a stream
    */
  case class AdHocDeviceSeed(sens : List[(String,Any,SensorStream)]) extends DeviceSeed[scafiWorld.DEVICE_PRODUCER] {
    /**
      * @return a sequence of scafi builder
      */
    override lazy val devices: Iterable[scafiWorld.DEVICE_PRODUCER] = sens. map { x => {
      x._2 match  {
        case led : Boolean => scafiWorld.LedProducer(x._1,led,x._3)
        case double : Double => scafiWorld.DoubleSensorValue(x._1,double,x._3)
        case _ => scafiWorld.GeneralSensorProducer(x._1,x._2,x._3)
      }
    }}
  }
}