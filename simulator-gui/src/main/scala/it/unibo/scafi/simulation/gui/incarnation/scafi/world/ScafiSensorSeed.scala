package it.unibo.scafi.simulation.gui.incarnation.scafi.world

import it.unibo.scafi.simulation.gui.configuration.SensorName._
import it.unibo.scafi.simulation.gui.model.sensor.SensorConcept.{SensorStream, sensorInput, sensorOutput}
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
  case object standardSeed extends ScafiSensorSeed {
    /**
      * @return a sequence of scafi builder
      */
    override lazy val sensor: Iterable[scafiWorld.DEVICE_PRODUCER] = List(scafiWorld.LedProducer(sensor1,false,sensorInput),
                                                                                scafiWorld.LedProducer(sensor2,false,sensorInput),
                                                                                scafiWorld.LedProducer(sensor3,false,sensorInput),
                                                                                scafiWorld.GeneralSensorProducer(name = output1,stream = sensorOutput))
  }

  case class AdHocSensorSeed (sens : List[(String,Any,SensorStream)]) extends ScafiSensorSeed {
    /**
      * @return a sequence of scafi builder
      */
    override lazy val sensor: Iterable[scafiWorld.DEVICE_PRODUCER] = sens. map {x => {
      x._2 match  {
        case led : Boolean => scafiWorld.LedProducer(x._1,led,x._3)
        case double : Double => scafiWorld.DoubleSensorValue(x._1,double,x._3)
        case _ => scafiWorld.GeneralSensorProducer(x._1,x._2,x._3)
      }
    }}
  }
}