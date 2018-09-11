package it.unibo.scafi.simulation.gui.model.sensor
import it.unibo.scafi.simulation.gui.model.aggregate.AggregateWorld
import it.unibo.scafi.simulation.gui.model.sensor.SensorConcept.{SensorStream, SensorType}

/**
  * describe the main set of sensor concept
  */
trait SensorConcept {
  self : SensorConcept.Dependency =>
  /**
    * sensor is a device that wrap a value
    * @tparam V the type of value
    */
  trait Sensor[V] <: self.Device {
    /**
      * @return the value of sensor
      */
    def value : V

    /**
      * @return the stream of sensor
      */
    def stream : SensorStream

    override def toString =  s"Sensor name = $name, value = $value, stream = $stream"
  }

  /**
    * a mutable sensor, used inside the world to change his state
 *
    * @tparam V the type of value
    */
  //noinspection AbstractValueInTrait
  protected trait MutableSensor[V] <: Sensor[V] with RootMutableDevice {
    /**
      * the value of sensor can change at run time
      */
    var value : V

    /**
      * a marked of sensor type
      * @return the sensor type
      */
    def sensorType : SensorType
  }

}
object SensorConcept {
  type Dependency = AggregateWorld
  private type MutableSensor = SensorConcept#MutableSensor[Any]

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
    def unapply(arg: Any): Option[SensorConcept#Sensor[_ <: Any]] = arg match {
      case sens : SensorConcept#Sensor[Any] => Some(sens)
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