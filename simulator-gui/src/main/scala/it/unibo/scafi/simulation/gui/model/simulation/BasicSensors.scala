package it.unibo.scafi.simulation.gui.model.simulation

object BasicSensors {

  /**
    * an on off sensor
    */
  object OnOffSensor {
    def unapply[E](arg: E): Option[Boolean] = {
      if(arg.isInstanceOf[BasicPlatform#Sensor[E]]) {
        val s = arg.asInstanceOf[BasicPlatform#Sensor[E]]
        if(s.value.isInstanceOf[Boolean]) return Some(s.value.asInstanceOf[Boolean])
      }
      None
    }
  }

  /**
    * a sensor with a text value to show
    */
  object DisplaySensor {
    def unapply[E](arg: E): Option[String] = {
      if(arg.isInstanceOf[BasicPlatform#Sensor[E]]) {
        val s = arg.asInstanceOf[BasicPlatform#Sensor[E]]
        if(s.value.isInstanceOf[String]) {
          val v = s.value.toString
          if(v.isEmpty) return None
          else return Some(s.value.asInstanceOf[String])
        }
      }
      None
    }

    /**
      * a sensor that has a double value
      */
    object DoubleSensor {
      def unapply[E](arg: E): Option[Double] = {
        if(arg.isInstanceOf[BasicPlatform#Sensor[E]]) {
          val s = arg.asInstanceOf[BasicPlatform#Sensor[E]]
          if(s.value.isInstanceOf[Double]) {
            return Some(s.value.asInstanceOf[Double])
          }
        }
        None
      }
    }}
}