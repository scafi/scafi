package it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.world

import it.unibo.scafi.simulation.s2.frontend.configuration.SensorName._
import it.unibo.scafi.simulation.s2.frontend.model.sensor.SensorConcept.SensorStream
import it.unibo.scafi.simulation.s2.frontend.model.sensor.SensorConcept.sensorInput
import it.unibo.scafi.simulation.s2.frontend.model.sensor.SensorConcept.sensorOutput

/**
 * describe a set of device producers in scafi context
 */
object ScafiDeviceProducers {

  /**
   * the standard set of device factory in a scafi aggregate program, the sensor is: 1 - sensor1 like a led device
   * (true/false) 2 - sensor2 like a led device (true/false) 3 - sensor3 like a led device (true/false) 4 - sensor4 like
   * a led device (true/false) 5,6,7,8 - output a sensor that show the actuation of scafi aggregate program, it is a
   * generic sensor (could be also a led device), this sensor is not visible by scafi simulation, it is used to graphics
   * purpose only
   */
  val standardConfiguration: Iterable[scafiWorld.DEVICE_PRODUCER] = List(
    scafiWorld.LedProducer(sensor1, value = false, sensorInput),
    scafiWorld.LedProducer(sensor2, value = false, sensorInput),
    scafiWorld.LedProducer(sensor3, value = false, sensorInput),
    scafiWorld.LedProducer(sensor4, value = false, sensorInput),
    scafiWorld.GeneralSensorProducer(name = output1, stream = sensorOutput),
    scafiWorld.GeneralSensorProducer(name = output2, stream = sensorOutput),
    scafiWorld.GeneralSensorProducer(name = output3, stream = sensorOutput),
    scafiWorld.GeneralSensorProducer(name = output4, stream = sensorOutput)
  )

  /**
   * ad hoc device configuration, you can pass a set of name, value and sensor stream that convert in device producers
   * iterable
   * @param sens
   *   the sensor describe in a list form
   * @return
   *   the device producers created
   */
  def ahHocDeviceConfiguration(sens: List[(String, Any, SensorStream)]): Iterable[scafiWorld.DEVICE_PRODUCER] =
    sens.map { x =>
      {
        x._2 match {
          case led: Boolean => scafiWorld.LedProducer(x._1, led, x._3)
          case double: Double => scafiWorld.DoubleSensorValue(x._1, double, x._3)
          case _ => scafiWorld.GeneralSensorProducer(x._1, x._2, x._3)
        }
      }
    }

}
