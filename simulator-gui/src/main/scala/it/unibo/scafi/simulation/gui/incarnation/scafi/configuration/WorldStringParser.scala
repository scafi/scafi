package it.unibo.scafi.simulation.gui.incarnation.scafi.configuration

import it.unibo.scafi.simulation.gui.configuration.command.CommandFactory.CommandArg
import it.unibo.scafi.simulation.gui.configuration.command.MoveCommandFactory.MoveArg
import it.unibo.scafi.simulation.gui.configuration.command.ToggleCommandFactory.ToggleArg
import it.unibo.scafi.simulation.gui.configuration.language.Language.StringCommandParser
import it.unibo.scafi.simulation.gui.model.space.{Point2D, Point3D}

/**
  * describe a string parser set used to create command that modify world
  */
object WorldStringParser {
  /**
    * used to create a toggle argument by a string
    */
  object ToggleStringParser extends StringCommandParser {
    private val regex = raw"toggle (\d+),(.*)".r
    override def parse: Option[CommandArg] = arg match {
      case regex(id,name) => Some(ToggleArg(name,Set(id.toInt)))
      case _ => None
    }

    override def help: String = "type toggle id,name to toggle a sensor value"
  }

  object MoveStringParser extends StringCommandParser {
    private val regex2d = raw"move (\d+),point\((\d+),(\d+)\)".r
    private val regex3d = raw"move (\d+),point\((\d+),(\d+),(\d+)\)".r
    override def parse: Option[CommandArg] = arg match {
      case regex2d(id,x,y) => Some(MoveArg(Map(id.toInt -> Point3D(x.toInt,y.toInt,0))))
      case regex3d(id,x,y,z) => Some(MoveArg(Map(id.toInt -> Point3D(x.toInt,y.toInt,z.toInt))))
      case _ => None
    }
    override def help: String = "type move id,point(x,y) or type move id,point(x,y,z) to move a node to position selected"
  }
}
