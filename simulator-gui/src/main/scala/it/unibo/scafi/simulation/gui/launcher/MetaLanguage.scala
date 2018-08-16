package it.unibo.scafi.simulation.gui.launcher

import it.unibo.scafi.simulation.gui.configuration.Configuration
import it.unibo.scafi.simulation.gui.configuration.language.{ConfigurationLanguage, OnlineLanguage}

class MetaLanguage(configLanguage : ConfigurationLanguage, config : Configuration[_,_], onlineLanguage: Option[OnlineLanguage] = None) {
  def run(): Unit = {
    println("Welcome!")
    while(!config.launched) {
      println(configLanguage.parse(readLine()))
    }
    while(onlineLanguage.isDefined) {
      println(onlineLanguage.get.parse(readLine()))
    }
  }

}
