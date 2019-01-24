package it.unibo.scafi.simulation.s2.frontend.model.sensor
import it.unibo.scafi.simulation.s2.frontend.model.aggregate.AggregateWorld
import it.unibo.scafi.simulation.s2.frontend.model.sensor.SensorConcept.{SensorStream, SensorType}

/**
  * describe the main set of sensor concept
  */
trait SensorConcept {
  self : SensorConcept.Dependency =>
  /**
    * sensor is a device that wrap a value
    */
  trait Sensor <: self.Device {
    /**
      * @return the value of sensor
      */
    def value[V] : V

    /**
      * @return the stream of sensor
      */
    def stream : SensorStream

    override def toString =  s"Sensor name = $name, value = $value, stream = $stream"
  }

  /**
    * a mutable sensor, used inside the world to change his state
 *
    */
  //noinspection AbstractValueInTrait
  protected trait MutableSensor <: Sensor with RootMutableDevice {
    /**
      * the value of sensor can change at run time
      */
    def value_=(sens : Any) : Any

    /**
      * a marked of sensor type
      * @return the sensor type
      */
    def sensorType : SensorType
  }

}
object SensorConcept {
  type Dependency = AggregateWorld
  private type MutableSensor = SensorConcept#MutableSensor

  /**
    * it used to typify a sensor
    */
  trait SensorType {
    /**
      * used to verify the value of sensor
      * @param arg a new value of sensor
      * @return true if the value is legit false otherwise
      */
    def accept[E](arg : E): Boolean
  }

  /**
    * pattern matching used to verify if an object is a sensor device
    */
  object SensorDevice {
    def unapply(arg: Any): Option[MutableSensor] = arg match {
      case sens : MutableSensor => Some(sens)
      case _ => None
    }
  }

  /**
    * describe the stream of sensor, can be inputStream or OutputStream
    * sensor stream has this task:
    * node can use sensor:
    *   as acquisition (the node check the value and produce a result)
    *   as writable output (after computation node change the state of a sensor of this type)
    */
  sealed trait SensorStream

  /**
    * sensor input stream
    */
  final object sensorInput extends SensorStream {
    override def toString: String = "input"
  }

  /**
    * sensor output stream
    */
  final object sensorOutput extends SensorStream {
    override def toString: String = "output"
  }
}