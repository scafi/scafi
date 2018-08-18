package it.unibo.scafi.simulation.gui.launcher

import it.unibo.scafi.simulation.gui.configuration.command.FieldParser.FieldValue
import it.unibo.scafi.simulation.gui.configuration.command.{CommandFactory, FieldParser}

/**
  * a launcher that create application with field passed
  */
class MetaFieldLauncher(val section : Map[FieldParser,CommandFactory],
                        val subsection : Map[String,Map[FieldParser,CommandFactory]],
                        val launchStrategy : () => String) {
  private val parsers = section.keySet ++ subsection.flatMap{_._2}.keySet
  private val factories = section.values ++ subsection.flatMap{_._2}.values

  def launch(fields : List[List[FieldValue]]) : String = {
    val commandArgs = fields.map{x => parsers.map {_.parse(x)}}.flatten.filter(_.isDefined).map(_.get)
    val commands = commandArgs.map {x => factories.map{_.create(x)}}.flatten.filter(_.isDefined).map(_.get)
    commands.foreach {_.make()}
    launchStrategy()
  }

}
