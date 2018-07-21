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
      * @return the stream of sensor, if it is acquisition sensor or it is a output sensor
      */
    def stream : SensorStream

    override def toString =  s"Sensor name = $name, value = $value, stream = $stream"
  }

  /**
    * a mutable sensor, used inside the world to change his state
    * @tparam V the type of value
    */
  protected trait MutableSensor[V] <: Sensor[V] with RootMutableDevice {

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
    type SENSOR

    /**
      * used for pattern matching
      * @param arg the value to match
      * @return None if arg isn't a sensor, the sensor value otherwise
      */
    def unapply(arg: Any): Option[SENSOR]

    /**
      * used to verify the value of sensor
      * @param arg a new value of sensor
      * @return true if the value is legit false otherwise
      */
    def accept[E](arg : E): Boolean
  }

  /**
    * method used to try to convert any type to mutable senor
    * @param sensor the sensor
    * @return None if the sensor is not a MutableSensor false other
    */
  def anyToMutable(sensor : Any) : Option[MutableSensor] = {
    if(!sensor.isInstanceOf[MutableSensor]) None
    else Some(sensor.asInstanceOf[MutableSensor])
  }

  /**
    * describe the stream of sensor, can be inputStream or OutputStream
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