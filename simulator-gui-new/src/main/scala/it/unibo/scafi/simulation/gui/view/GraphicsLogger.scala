package it.unibo.scafi.simulation.gui.view

/**
  * utility to set information on graphics log
  * you can change graphics configuration associated to
  * some channel like this:
  * <pre>
  *  {@code
  *     GraphicsLogger.addStrategy("output",LogType.textual
  *  }
  * </pre>
  */
object GraphicsLogger {

  private [view] var strategyType : Map[String,LogType] = Map.empty

  /**
    * ad a strategy to show log information
    * @param channel the channel
    * @param logType the log strategy type
    */
  def addStrategy(channel : String, logType: LogType) : Unit = strategyType += channel -> logType

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

  /**
    * a tree visualization of log
    */
  case object treeView extends LogType
  val logTypes : Seq[LogType] = textual :: lineChart :: Nil
}
