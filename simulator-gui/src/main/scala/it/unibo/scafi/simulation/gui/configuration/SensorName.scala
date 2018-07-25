package it.unibo.scafi.simulation.gui.configuration

/**
  * a space used to define a set of sensor name
  */
object SensorName {
  trait Name {
    def name : String
  }

  /**
    * all sensor name accept
    */
  val sensor1 : Name = new Name{val name = "sens1"}
  val sensor2 : Name = new Name{val name = "sens2"}
  val sensor3 : Name = new Name{val name = "sens3"}
  val output1 : Name = new Name{val name = "output"}
  val output2 : Name = new Name{val name = "output1"}
  val output3 : Name = new Name{val name = "output2"}

}
