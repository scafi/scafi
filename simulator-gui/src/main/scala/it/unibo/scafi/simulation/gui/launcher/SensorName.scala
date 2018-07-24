package it.unibo.scafi.simulation.gui.launcher

object SensorName {
  trait Name {
    val name : String
  }

  /**
    * all sensor name accept
    */
  val sens1 : Name = new Name{val name = "sens1"}
  val sens2 : Name = new Name{val name = "sens2"}
  val sens3 : Name = new Name{val name = "sens3"}
  val gsensor : Name = new Name{val name = "value"}
  val gsensor1 : Name = new Name{val name = "output"}
  val gsensor2 : Name = new Name{val name = "generic2"}

}
