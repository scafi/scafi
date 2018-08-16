package it.unibo.scafi.simulation.gui.configuration.language

import it.unibo.scafi.simulation.gui.configuration.command.{Command, CommandFactory}
import it.unibo.scafi.simulation.gui.configuration.command.CommandFactory.CommandParser
import it.unibo.scafi.simulation.gui.configuration.language.Language.StringCommandParser

/**
  * describe a way to use string to create a set of action
  */
trait Language {
  /**
    * the implementation descre how to populate this map
    * @return the map of string command parser and command factory
    */
  protected def parsers : Map[StringCommandParser,CommandFactory]

  /**
    * the logic of computation
    * @param commands the command to process
    */
  protected def computeCommand(commands : Command) : String
  final def parse(commands : String) : String = commands.split(";").map(command => {
    if(command == Language.Help) {
      parsers.keySet.map {_.help}.reduce((x,y) => x + "\n" + y)
    } else if (command == Language.Description) {
      parsers.values.map {x => x.name + " " + CommandFactory.CommandFactoryDescription.descriptionCommand(x.name)}.reduce((x,y) => x + "\n" + y)
    } else {
      parsers.keySet.foreach {x => x.arg = command}
      val commandProduced = parsers.map {x => x._2 -> x._1.parse}
        .filter {x => x._2.isDefined}
        .map {x => x._1.create(x._2.get)}
        .filter {_.isDefined}
        .map {_.get}
        .toList
      if(commandProduced.isEmpty) {
        Language.Wrong
      } else {
        commandProduced.map{computeCommand(_)}.last
      }
    }
  }).mkString("\n")
}
object Language {
  private [language] val Help = "help"
  private [language] val Description = "description"
  private [language] val Ok = "+"
  private [language] val Wrong = "no command found, type help or description to see all command"
  trait StringCommandParser extends CommandParser[String] {
    var arg : String = ""
  }
}