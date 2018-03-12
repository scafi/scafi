package it.unibo.scafi.simulation.gui.model.simulation

object BasicSensors {
  object OnOffSensor {
    def unapply[E](arg: Any): Option[Boolean] = {
      if(arg.isInstanceOf[BasicPlatform#Sensor[E]]) {
        val s = arg.asInstanceOf[BasicPlatform#Sensor[E]]
        if(s.value.isInstanceOf[Boolean]) return Some(s.value.asInstanceOf[Boolean])
      }
      None
    }
  }
  object TextSensor {
    def unapply[E](arg: Any): Option[String] = {
      if(arg.isInstanceOf[BasicPlatform#Sensor[E]]) {
        val s = arg.asInstanceOf[BasicPlatform#Sensor[E]]
        if(s.value.isInstanceOf[String]) {
          return Some(s.value.asInstanceOf[String])
        }
      }
      None
    }
  }
}