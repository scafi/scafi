package it.unibo.scafi.simulation.gui.configuration.logger

import it.unibo.scafi.simulation.gui.controller.logger.LogManager

/**
  * allow to configure log system
  */
trait LogConfiguration {
  def apply()
}

object LogConfiguration {
  import LogManager._

  /**
    * no log used in the application
    */
  object noLog extends LogConfiguration {
    override def apply(): Unit = {}
  }

  /**
    * a standard configuration used log in standard out and in different file name
    * called as log chanell
    */
  object standardLog extends LogConfiguration {
    override def apply(): Unit = {
      val consoleTimestampOutput = new ConsoleTimestampOutputObserver
      val consoleOutput = new ConsoleOutputObserver
      FileOutputObserver.acceptChannel(acceptExceptThese(Set(Channel.CommandResult)))
      consoleTimestampOutput.acceptChannel(acceptThese(Set(Channel.Error)))
      consoleOutput.acceptChannel(acceptThese(Set(Channel.CommandResult)))
      LogManager.attach(consoleOutput)
      LogManager.attach(FileOutputObserver)
      LogManager.attach(consoleTimestampOutput)
    }
  }
}