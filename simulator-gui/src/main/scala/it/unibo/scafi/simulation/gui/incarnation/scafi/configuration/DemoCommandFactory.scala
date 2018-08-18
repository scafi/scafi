package it.unibo.scafi.simulation.gui.incarnation.scafi.configuration

import it.unibo.scafi.simulation.gui.configuration.command.Command.onlyMakeCommand
import it.unibo.scafi.simulation.gui.configuration.command.CommandFactory.CommandArg
import it.unibo.scafi.simulation.gui.configuration.command.CommandFactory.CommandFactoryName.CommandName
import it.unibo.scafi.simulation.gui.configuration.command.FieldParser.{Field, FieldValue, MultipleField}
import it.unibo.scafi.simulation.gui.configuration.command.{Command, CommandFactory, FieldParser}
import it.unibo.scafi.simulation.gui.configuration.language.Language.StringCommandParser
import it.unibo.scafi.simulation.gui.demo
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.reflection.Demo
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.DemoCommandFactory.{ListDemo, SetDemo}
import it.unibo.scafi.simulation.gui.util.Result.{Fail, Success}

/**
  * is a factory used to create command that allow to set demo and show all demo supported
  * @param scafiConfiguration the scafiConfiguration used to save command result
  */
class DemoCommandFactory(scafiConfiguration: ScafiConfiguration) extends CommandFactory{
  override def name: CommandName = CommandFactory.CommandFactoryName.Demo

  override def create(arg: CommandFactory.CommandArg): Option[Command] = arg match {
    case SetDemo(demo) => Some(onlyMakeCommand(() => {
      if(demo.isAnnotationPresent(classOf[Demo])) {
        scafiConfiguration.demo = Some(demo)
        Success
      } else {
        Fail(DemoCommandFactory.IllegalClass)
      }
    }))
    case ListDemo => Some(onlyMakeCommand(() => {
      demo.demos.foreach {x => println(x.getSimpleName)}
      Success
    }))
    case _ => None
  }
}

object DemoCommandFactory {
  protected val IllegalClass = "class is not legit to simulation"
  private val regex = raw"demo=(.*)".r

  /**
    * command argument used to set demo
    * @param demo the demo class
    */
  case class SetDemo(demo : Class[_]) extends CommandArg

  /**
    * command argument used to list demo supported
    */
  object ListDemo extends CommandArg

  /**
    * demo string parser used to parse a string to DemoArg
    */
  object DemoStringParser extends StringCommandParser{

    override def parse(arg : String): Option[CommandArg] = arg match {
      case "list demo" => Some(ListDemo)
      case regex(demoClass) => if(demo.nameToDemoClass.get(demoClass).isDefined) Some(SetDemo(demo.nameToDemoClass(demoClass))) else None
      case _ => None
    }

    override def help: String = "type list demo to see al demo \ntype demo=demoClass to set the current demo"
  }

  object DemoFieldParser extends FieldParser {
    override def fields: Set[FieldParser.Field] = Set(Field("demos",MultipleField(demo.demos.map {x => x.getSimpleName}toSet)))

    override def parse(arg: Iterable[FieldParser.FieldValue]): Option[CommandArg] = {
      var d : Option[Class[_]] = None
      arg foreach { _ match {
        case FieldValue(name @ "demos", v : String) => if(demo.nameToDemoClass.get(v).isDefined) d = Some(demo.nameToDemoClass(v))
        case _ => None
      }}
      if(d.isDefined) Some(SetDemo(d.get))
      else None
    }

    override def help: String = ""
  }
}
