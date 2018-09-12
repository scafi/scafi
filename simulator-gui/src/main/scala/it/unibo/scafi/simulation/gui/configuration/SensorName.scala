package it.unibo.scafi.simulation.gui.configuration

/**
  * a space used to define a set of sensor name
  */
object SensorName {
  /**
    * all sensor name accept
    */
  var sensor1 : String = "sens1"
  var sensor2 : String = "sens2"
  var sensor3 : String = "sens3"
  var sensor4 : String = "sens4"
  var output1 : String = "output1"
  var output2 : String = "output2"
  var output3 : String = "output3"
  var output4 : String = "output4"
  /**
    * return the name of all input sensor
    */
  val inputSensor = List(sensor1,sensor2,sensor3,sensor4)
  /**
    * return the name of all output sensor
    */
  val outputSensor = List(output1,output2,output3)

  /**
    * a method used to change sensor namespace
    * @param sensor1 sensor1 name
    * @param sensor2 sensor2 name
    * @param sensor3 sensor3 name
    */
  def sensorNamespace(sensor1 : String = sensor1,
                      sensor2 : String = sensor2,
                      sensor3 : String = sensor3,
                      sensor4 : String = sensor4) : Unit = {
    this.sensor1 = sensor1
    this.sensor2 = sensor2
    this.sensor3 = sensor3
    this.sensor4 = sensor4
  }
}
