package it.unibo.scafi.simulation.gui.launcher

import it.unibo.scafi.simulation.gui.configuration.command.CommandSpace
import it.unibo.scafi.simulation.gui.controller.input.Command.{CommandDescription, Fail, Success}
import it.unibo.scafi.simulation.gui.controller.input.inputCommandController

/**
  * a class used to describe a console language to launch a simulation and to modify the world state
  */
class MetaConsoleApplication(consoleConfigurator: ConsoleConfigurator, spaces : CommandSpace[CommandDescription] *) {
  require(consoleConfigurator!=null && spaces != null)
  private val CommandDontFound = "Command don't found, try to hit help or description"

  /**
    * run the console language that parse a string and actuate the changes
    */
  final def run(): Unit = {
    println("Welcome!")
    worldConfiguration()
    afterLaunch()
  }

  private def worldConfiguration(): Unit = {
    while(!consoleConfigurator.launched) {
      val line = readLine()
      if(!verifyHelpOrDescription(line,consoleConfigurator)) {
        val command = consoleConfigurator.fromString(line)
        if(command.isDefined) {
          println(command.get.make() match {
            case Success => "+"
            case Fail(e) => "- : " + e
          })
        } else {
          println(CommandDontFound)
        }
      }
    }
  }

  private def afterLaunch() : Unit = {
    while(true) {
      val line = readLine()
      if(!verifyHelpOrDescription(line,spaces:_*)) {
        val command = spaces.map(_.fromString(line)).filter(_.isDefined)
        if(command.nonEmpty) {
          inputCommandController.exec(command.head.get)
        } else {
          println(CommandDontFound)
        }
      }
    }
  }
  private def verifyHelpOrDescription (line : String, commandSpace: CommandSpace[_ <: CommandDescription] *): Boolean = {
    if(line == "help") {
      for (space <- commandSpace) {
        for(description <- space.descriptors) {
           println(description.help)
        }
      }
      true
    } else if (line == "description") {

      for (space <- commandSpace) {
        for(description <- space.descriptors) {
          println(description.description)
        }
      }
      true
    } else {
      false
    }
  }
}
