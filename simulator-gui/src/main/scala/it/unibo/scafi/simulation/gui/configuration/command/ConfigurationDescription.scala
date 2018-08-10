package it.unibo.scafi.simulation.gui.configuration.command

import it.unibo.scafi.simulation.gui.configuration.command.ConfigurationDescription.{Field, FieldValue}
import it.unibo.scafi.simulation.gui.controller.input.Command
import it.unibo.scafi.simulation.gui.controller.input.Command.CommandDescription

abstract class ConfigurationDescription(help : String, description: String, name : String) extends CommandDescription(help,description) {
  /**
    * try to parse the value passed to create a command
    * @param values field passed
    * @return None if value is not legit some of command descripted otherwise
    */
  def parseFromField(values : FieldValue[Any] *) : Option[Command]

  /**
    * all field accepted into configuration description
    * @return
    */
  def fields : Iterable[Field]
}

object ConfigurationDescription {

  /**
    * a type used to create a configuration description
    */
  sealed trait ConfigurationType
  /**
    * a type that describe a string
    */
  case object stringType extends ConfigurationType

  /**
    * a type that describe a number
    */
  case object intType extends ConfigurationType

  /**
    * describe a finite mutiple value
    * @param value the value accept
    */
  case class multipleType[E](value : E*) extends ConfigurationType

  /**
    * describe a field used to initialize crete a command in a configuration description
    * @param name the field name
    * @param configurationType the type of field
    */
  case class Field(name : String, configurationType: ConfigurationType)

  /**
    * describe a value associated to a field
    * @param name the field name
    * @param value the value of field
    * @tparam E the type of field
    */
  case class FieldValue[E](name : String, value : E*)
}
