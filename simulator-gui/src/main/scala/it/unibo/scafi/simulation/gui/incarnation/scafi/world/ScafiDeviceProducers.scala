package it.unibo.scafi.simulation.gui.incarnation.scafi.world

import it.unibo.scafi.simulation.gui.configuration.SensorName._
import it.unibo.scafi.simulation.gui.model.sensor.SensorConcept.{SensorStream, sensorInput, sensorOutput}

/**
  * describe a set of device seed in scafi context
  */
object ScafiDeviceProducers {

  /**
    * set of device producers seed factory
    */
  val standardConfiguration : Iterable[scafiWorld.DEVICE_PRODUCER] = List(scafiWorld.LedProducer(sensor1,false,sensorInput),
    scafiWorld.LedProducer(sensor2,false,sensorInput),
    scafiWorld.LedProducer(sensor3,false,sensorInput),
    scafiWorld.GeneralSensorProducer(name = output1,stream = sensorOutput))

  /**
    * ad hoc divece configuration, you can pass a set of name, value and sensor stream that
    * convert in device producers iterable
    * @param sens the sensor describe in a list form
    * @return the device producers created
    */
  def ahHocDeviceConfiguration(sens : List[(String,Any,SensorStream)]) : Iterable[scafiWorld.DEVICE_PRODUCER] = sens.map { x => {
    x._2 match  {
      case led : Boolean => scafiWorld.LedProducer(x._1,led,x._3)
      case double : Double => scafiWorld.DoubleSensorValue(x._1,double,x._3)
      case _ => scafiWorld.GeneralSensorProducer(x._1,x._2,x._3)
    }
  }}

}