package it.unibo.scafi.simulation.gui.model.sensor.implementation.mutable

import it.unibo.scafi.simulation.gui.model.sensor.SensorConcept
import it.unibo.scafi.simulation.gui.model.sensor.SensorConcept.{SensorStream, SensorType}
import it.unibo.scafi.simulation.gui.model.sensor.implementation.mutable.SensorDefinition.{DoubleSensor, General, Led}

/**
  * describe a principle sensor definition
  */
trait SensorDefinition extends SensorConcept {
  self : SensorDefinition.Dependency =>

  override type SENSOR_VALUE = Any

  override type DEVICE = Sensor[SENSOR_VALUE]

  override protected type MUTABLE_DEVICE = MutableSensor[SENSOR_VALUE]

  override type DEVICE_PRODUCER = DeviceProducer

  /**
    * an implementation of a mutable sensor
    * @param name the name of sensor
    * @param sensorValue the initial sensor value
    * @param sensorType the sensor type, used to check the correctness of new value
    * @param stream the stream of sensor
    */
  protected class MutableSensorImpl(val name: NAME, sensorValue : SENSOR_VALUE, val sensorType: SensorType, val stream : SensorStream) extends MutableSensor[SENSOR_VALUE] {
    private var _val = sensorValue
    require(name != null && sensorValue != null && sensorType != null)
    override def view: DEVICE = this


    def value_=(newValue: SENSOR_VALUE) = {
      require(newValue != null && sensorType.accept(newValue))
      _val = newValue
    }

    def value : SENSOR_VALUE = _val

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
    * @param name the name of led
    * @param v the initial value
    */
  final case class LedProducer(name : NAME, v : Boolean, stream : SensorStream) extends DeviceProducer {
    override def build: MUTABLE_DEVICE = new MutableSensorImpl(name,v,Led,stream)
  }

  /**
    * a general sensor that can wrap every value
    * @param name name of sensor
    * @param v the initial value
    */
  final case class GeneralSensorProducer(name : NAME, v : Any, stream : SensorStream) extends DeviceProducer {
    override def build: MUTABLE_DEVICE = new MutableSensorImpl(name,v,General,stream)
  }

  /**
    * a sensor that has a generic float value
    * @param name the name of sensor
    * @param v the initial value
    */
  final case class DoubleSensorValue(name : NAME, v : Double, stream : SensorStream) extends DeviceProducer {
    override def build: MUTABLE_DEVICE = new MutableSensorImpl(name,v,DoubleSensor, stream)
  }
}

object SensorDefinition {
  import reflect.runtime.universe._
  type Dependency = SensorWorld
  /**
    * a set of sensor type
    */

  //pattern mateching used to check is sensor is led
  object Led extends SensorType {
    override type SENSOR = Boolean

    def unapply(arg: Any): Option[SENSOR] = {
      val optionSensor = SensorConcept.anyToMutable(arg)
      if(optionSensor.isEmpty) return None
      val sensor = optionSensor.get
      sensor.sensorType match {
        case Led => Some(sensor.value.asInstanceOf[Boolean])
        case _ => None
      }
    }

    override def accept[E](arg: E): Boolean = arg.isInstanceOf[SENSOR]
  }
  //pattern matching used to check is sensor has a generic type
  object General extends SensorType {
    override type SENSOR = Any
    def unapply(arg: Any): Option[Any] = {
      val optionSensor = SensorConcept.anyToMutable(arg)
      if(optionSensor.isEmpty) return None
      val sensor = optionSensor.get
      sensor.value match {
        case General => Some(sensor.value.asInstanceOf[Boolean])
        case _ => None
      }
    }
    override def accept[E](arg: E): Boolean = true
  }
  //pattern matching used to check is sensor has a float value
  object DoubleSensor extends SensorType {
    override type SENSOR = Double
    def unapply(arg: Any): Option[SENSOR] = {
      val optionSensor = SensorConcept.anyToMutable(arg)
      if(optionSensor.isEmpty) return None
      val sensor = optionSensor.get
      sensor.sensorType match {
        case DoubleSensor => Some(sensor.value.asInstanceOf[SENSOR])
        case _ => None
      }
    }
    override def accept[E](arg: E): Boolean = arg.isInstanceOf[Number]

  }
}
