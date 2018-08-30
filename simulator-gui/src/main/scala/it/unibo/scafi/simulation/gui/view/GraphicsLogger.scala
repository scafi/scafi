package it.unibo.scafi.simulation.gui.view

/**
  * utility to set information on graphics log
  */
object GraphicsLogger {

  private [view] var strategyType : Map[String,LogType] = Map.empty

  /**
    * ad a strategy to show log information
    * @param channel the channel
    * @param logType the log strategy type
    */
  def addStrategy(channel : String, logType: LogType) = strategyType += channel -> logType

  /**
    * root type of log type
    */
  sealed trait LogType

  /**
    * a textual logging
    */
  case object textual extends LogType

  /**
    * a line chart like logging
    */
  case object lineChart extends LogType

  val logTypes : Seq[LogType] = textual :: lineChart :: Nil
}
