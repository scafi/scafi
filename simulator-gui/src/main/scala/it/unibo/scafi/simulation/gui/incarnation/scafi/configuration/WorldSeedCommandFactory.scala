package it.unibo.scafi.simulation.gui.incarnation.scafi.configuration

import it.unibo.scafi.simulation.gui.configuration.command.Command.onlyMakeCommand
import it.unibo.scafi.simulation.gui.configuration.command.CommandFactory.CommandArg
import it.unibo.scafi.simulation.gui.configuration.command.CommandFactory.CommandFactoryName.CommandName
import it.unibo.scafi.simulation.gui.configuration.command.{Command, CommandFactory}
import it.unibo.scafi.simulation.gui.configuration.language.Language.StringCommandParser
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.WorldSeedCommandFactory.WorldSeedArg
import it.unibo.scafi.simulation.gui.model.core.Shape
import it.unibo.scafi.simulation.gui.model.graphics2D.BasicShape2D.{Circle, Rectangle}
import it.unibo.scafi.simulation.gui.util.Result.Success

/**
  * a command factory used to create command that create a scafi world seed
  * @param scafiConfiguration the configuration used to save the command result
  */
class WorldSeedCommandFactory(scafiConfiguration: ScafiConfiguration) extends CommandFactory{
  override def name: CommandName = CommandFactory.CommandFactoryName.WorldSeed
  override def create(arg: CommandFactory.CommandArg): Option[Command] = arg match {
    case WorldSeedArg(s) => {
      Some(onlyMakeCommand(() => {
        scafiConfiguration.worldSeed = Some(ScafiSeed(shape = Some(s)))
        Success
      }))
    }
  }
}

object WorldSeedCommandFactory {

  /**
    * command argument used to create a world seed
    * @param shape the shape of node in scafi world
    */
  case class WorldSeedArg(shape : Shape) extends CommandArg

  /**
    * a parser that parse a string into a world seed argument
    */
  object WorldSeedStringParser extends StringCommandParser {
    val regex = raw"worldSeed=seed\{shape=(.*)\}".r
    override def parse(arg : String): Option[CommandFactory.CommandArg] = arg match {
      case regex(stringShape) => {
        val shape = toShape(stringShape)
        if(shape.isEmpty) None
        else Some(WorldSeedArg(shape.get))
      }
      case _ => None
    }

    override def help: String = "use worldSeed=seed{shape=x} (shape can be rectangle(w,h), circle(r)...) to create a world seed\n use worldSeed=seed{shape=x,boundary={x,"
  }

  private def toShape(stringShape : String) : Option[Shape] = {
    val rectangle = raw"rectangle\((\d+),(\d+)\)".r
    val circle = raw"circle\((\d+)\)".r
    stringShape match {
      case rectangle(w,h) => Some(Rectangle(w.toInt,h.toInt))
      case circle(r) => Some(Circle(r.toInt))
      case _ => None
    }
  }
}
