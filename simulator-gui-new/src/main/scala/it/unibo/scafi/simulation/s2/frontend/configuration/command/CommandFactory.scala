package it.unibo.scafi.simulation.s2.frontend.configuration.command

import it.unibo.scafi.simulation.s2.frontend.configuration.command.Command.onlyMakeCommand
import it.unibo.scafi.simulation.s2.frontend.configuration.launguage.ResourceBundleManager
import it.unibo.scafi.simulation.s2.frontend.configuration.launguage.ResourceBundleManager._
import it.unibo.scafi.simulation.s2.frontend.util.Result
import it.unibo.scafi.simulation.s2.frontend.util.Result.{Fail, Success}
/**
  * a factory used to create a set of command
  * with a factory you can create command like these:
  *
  * <pre>
  *    {@code
  *       val creationResult = factory.create(Map("arg1" -> value1, "arg2" -> value"))
  *    }
  * </pre>
  *
  * factories isn't used directly (in general) but it is used for example in
  * @see it.unibo.scafi.simulation.gui.configuration.parser.UnixLikeParser
  */
trait CommandFactory {
  import CommandFactory._

  implicit val descriptionFile : String = ResourceBundleManager.KeyFile.CommandDescription
  private def wrongElementNumber : Fail[String] = Fail(international("wrong-element")(KeyFile.Error))

  /**
    * @return the command name
    */
  def name : String

  /**
    * @return command description
    */
  def description : String = international(name)

  /**
    * this method return a sequence of command argument that this factory accept to create command
    * @return a sequence of command argument description
    */
  def commandArgsDescription : Seq[CommandArgDescription]

  /**
    * this method is used to create a command,
    * the argument is a map of String -> Any, the factory
    * check if the argument if accepted and create the command
    * associated with the factory
    * @param args the command argument
    * @return (Success,Some(Command)) if the arguments are accepted (Fail,None) otherwise
    */
  final def create(args : CommandArg) : CreationResult = {
    if(args.size < commandArgsDescription.count(!_.optional) || args.size > commandArgsDescription.length) {
      (wrongElementNumber,None)
    } else {
      createPolicy(args)
    }
  }

  /**
    * a strategy defined by command factory implementation
    * @param args the command args
    * @return (Success,Some(Command)) if the arguments are accepted (Fail,None) otherwise
    */
  //TEMPLATE METHOD
  protected def createPolicy(args : CommandArg) : (Result,Option[Command])
}

object CommandFactory {
  /**
    * used to create empty arg parameter used to create command
    * @tparam A the type of first element
    * @tparam B the type of second element
    * @return empty map
    */
  def emptyArg[A,B] : Map[A,B] = Map.empty
  import ResourceBundleManager._
  /**
    * the type of command arg
    */
  type CommandArg = Map[String,Any]

  /**
    * the type of creation method
    */
  type CreationResult = (Result,Option[Command])

  /**
    * this case class is used to describe a command argument
    * @param name the argument name
    * @param valueType the argument type
    * @param optional a boolean value that tells if the argument is optional or not
    * @param description a description of argument
    * @param defaultValue the default value associated with the command argument
    */
  case class CommandArgDescription(name : String,
                                   valueType: ValueType,
                                   optional: Boolean = false,
                                   description: String = "",
                                   defaultValue: Option[Any] = None)

  /**
    * describe argument type accepted
    */
  sealed trait ValueType

  /**
    * int value type
    */
  object IntType extends ValueType {
    override def toString: String = "int"
  }

  /**
    * double value type
    */
  object DoubleType extends ValueType {
    override def toString: String = "double"
  }
  /**
    * string value type
    */
  object StringType extends ValueType {
    override def toString: String = "string"
  }

  /**
    * boolean type
    */
  object BooleanType extends ValueType {
    override def toString: String = "boolean"
  }

  /**
    * any value type
    */
  object AnyType extends ValueType {
    override def toString: String = "any"
  }

  /**
    * describe a finite set of value that the argument accept
    * @param value the arguments accepted
    */
  case class LimitedValueType(value : Any * ) extends ValueType {
    override def toString: String = "values=" + value.mkString(" ")
  }

  /**
    * the argument has a sequence of value associated
    * @param value the type of value
    */
  case class MultiValue(value : ValueType) extends ValueType {
    override def toString: String = "sequence of " + value.toString
  }

  /**
    * the argument is another map that map a value type to another
    * @param key the key type
    * @param value the value type
    */
  case class MapValue(key : ValueType, value : ValueType) extends ValueType {
    override def toString: String = "map of key " + key + " and value" + value
  }

  /**
    * this method is used to create a string when the parameter name is wrong
    * @param expected the parameter names expected into the command arg
    * @return the string created
    */
  def wrongParameterName(expected : String *) : String = {
    implicit val file : String  = KeyFile.Configuration
    val pass = i"pass"
    val runCommand = i"run-command"
    s"$pass ${expected.mkString(",")} $runCommand"
  }

  /**
    * this method is used to create a string when the parameter type is wrong
    * @param expected the parameter type expected
    * @param parameter the name of parameter
    * @return the string created
    */
  def wrongTypeParameter(expected : ValueType, parameter: String) : String = {
    implicit val file : String = KeyFile.Configuration
    val parameter = i"parameter"
    val isType = i"is-type"
    s"$parameter $parameter $isType $expected"
  }

  /**
    * this method is used to create creation result in a fast way
    * @param commandLogic the internal logic of command
    * @return the creation result created
    */
  def easyResultCreation(commandLogic : () => Unit) : CreationResult = (Success,Some(onlyMakeCommand(() => {
    commandLogic()
    Success
  })))

  /**
    * utility method used to create a creation result when command creation is failed
    * @param failReason the fail reason of creation
    * @return (Fail passed,None)
    */
  def creationFailed(failReason : Fail[_]) : CreationResult = (failReason,None)

  /**
    * utility method used to create a creation result when command creation is made
    * @param command the command created
    * @return (Success,command passed)
    */
  def creationSuccessful(command : Command) : CreationResult = (Success,Some(command))
}
