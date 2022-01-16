package it.unibo.scafi.simulation.s2.frontend.model.simulation.implementation.mutable

import it.unibo.scafi.simulation.s2.frontend.model.sensor.SensorConcept.SensorStream
import it.unibo.scafi.simulation.s2.frontend.model.sensor.SensorConcept.SensorType
import it.unibo.scafi.simulation.s2.frontend.model.sensor.SensorConcept
import it.unibo.scafi.simulation.s2.frontend.model.sensor.SensorWorld
import it.unibo.scafi.simulation.s2.frontend.model.simulation.implementation.mutable.SensorDefinition.DoubleSensor
import it.unibo.scafi.simulation.s2.frontend.model.simulation.implementation.mutable.SensorDefinition.General
import it.unibo.scafi.simulation.s2.frontend.model.simulation.implementation.mutable.SensorDefinition.Led

/**
 * describe main sensor definition
 */
trait SensorDefinition extends SensorConcept {
  self: SensorDefinition.Dependency =>

  override type DEVICE = Sensor

  override protected type MUTABLE_DEVICE = MutableSensor

  override type DEVICE_PRODUCER = DeviceProducer

  /**
   * an implementation of a mutable sensor
   * @param name
   *   the name of sensor
   * @param sensorValue
   *   the initial sensor value
   * @param sensorType
   *   the sensor type, used to check the correctness of new value
   * @param stream
   *   the stream of sensor
   */
  protected class MutableSensorImpl(
      val name: NAME,
      sensorValue: Any,
      val sensorType: SensorType,
      val stream: SensorStream
  ) extends MutableSensor {
    private var _val = sensorValue
    require(name != null && sensorValue != null && sensorType != null)
    override def view: DEVICE = this

    def value_=(newValue: Any): Unit = {
      require(newValue != null && sensorType.accept(newValue))
      _val = newValue
    }

    override def value[V]: V = _val.asInstanceOf[V]

    def canEqual(other: Any): Boolean = other.isInstanceOf[MutableSensorImpl]

    override def equals(other: Any): Boolean = other match {
      case that: MutableSensorImpl =>
        (that canEqual this) &&
          name == that.name &&
          sensorType == that.sensorType &&
          stream == that.stream
      case _ => false
    }

    override def hashCode(): Int = {
      val state = Seq(name, sensorType, stream)
      state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
    }
  }
  /**
   * a producer of led sensor, it can be on or off
   * @param name
   *   the name of led
   * @param value
   *   the initial value
   */
  final case class LedProducer(name: NAME, value: Boolean = false, stream: SensorStream) extends DeviceProducer {
    override def build: MUTABLE_DEVICE = new MutableSensorImpl(name, value, Led, stream)
  }

  /**
   * a general sensor that can wrap every value
   * @param name
   *   name of sensor
   * @param v
   *   the initial value
   */
  final case class GeneralSensorProducer(name: NAME, v: Any = "", stream: SensorStream) extends DeviceProducer {
    override def build: MUTABLE_DEVICE = new MutableSensorImpl(name, v, General, stream)
  }

  /**
   * a sensor that has a generic float value
   * @param name
   *   the name of sensor
   * @param v
   *   the initial value
   */
  final case class DoubleSensorValue(name: NAME, v: Double = 0.0, stream: SensorStream) extends DeviceProducer {
    override def build: MUTABLE_DEVICE = new MutableSensorImpl(name, v, DoubleSensor, stream)
  }
}

object SensorDefinition {
  type Dependency = SensorWorld
  /**
   * a set of sensor type
   */
  // strategy to verify if the value is correct
  object Led extends SensorType {
    override def accept[E](arg: E): Boolean = arg.isInstanceOf[Boolean]
  }
  // strategy to verify if the value is correct
  object General extends SensorType {
    override def accept[E](arg: E): Boolean = true
  }
  // strategy to verify if the value is correct
  object DoubleSensor extends SensorType {
    override def accept[E](arg: E): Boolean = arg.isInstanceOf[Number]
  }
}
