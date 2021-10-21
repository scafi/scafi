package it.unibo.scafi.simulation.s2.frontend.configuration.logger

import it.unibo.scafi.simulation.s2.frontend.controller.logger.LogManager
import it.unibo.scafi.simulation.s2.frontend.view.scalaFX
import it.unibo.scafi.simulation.s2.frontend.view.scalaFX.logger.FXLogger

/**
  * allow to configure log system
  */
trait LogConfiguration {
  def apply(): Unit
}

object LogConfiguration {
  import LogManager._

  /**
    * no log used in the application
    */
  case object NoLog extends LogConfiguration {
    override def apply(): Unit = {}
  }

  /**
    * a standard configuration used log in standard out and in different file name
    * called as log chanel
    */
  case object StandardLog extends LogConfiguration {
    override def apply(): Unit = {
      val consoleTimestampOutput = new ConsoleTimestampOutputObserver
      val consoleOutput = new ConsoleOutputObserver
      FileOutputObserver.acceptChannel(acceptExceptThese(Set(Channel.CommandResult,Channel.Export)))
      consoleTimestampOutput.acceptChannel(acceptThese(Set(Channel.Error)))
      consoleOutput.acceptChannel(acceptThese(Set(Channel.CommandResult)))
      LogManager.attach(consoleOutput)
      LogManager.attach(FileOutputObserver)
      LogManager.attach(consoleTimestampOutput)
    }
  }

  /**
    * this configuration show log in a graphics form
    */
  case object GraphicsLog extends LogConfiguration {
    override def apply(): Unit = {
      LogConfiguration.StandardLog()
      scalaFX.initializeScalaFXPlatform()
      FXLogger.acceptChannel(LogManager.acceptAll)
      LogManager.attach(FXLogger)
    }
  }
}