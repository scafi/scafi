package it.unibo.scafi.simulation.gui.configuration

/**
  * a configuration used to configure a program
  */
trait Configuration

object Configuration {

  /**
    * a configuration builder used to create configuration
    * @tparam C the configuration type
    */
  trait ConfigurationBuilder[C <: Configuration]{
    /**
      * tell if the builder build configuration or not
      * @return true if the configuration is created false otherwise
      */
    def created : Boolean

    /**
      * try to create the configuration
      * @return None if builder can't create configuration Some(configuration) otherwise
      */
    def create() : Option[C]
  }
}
