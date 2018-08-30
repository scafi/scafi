package it.unibo.scafi.simulation.gui.view.scalaFX.logger

import it.unibo.scafi.simulation.gui.controller.logger.LogManager.{IntLog, Log, LogObserver}
import it.unibo.scafi.simulation.gui.view.GraphicsLogger

import scalafx.scene.control.TabPane

/**
  * standard fx graphics logger
  */
object FXLogger extends TabPane with LogObserver {
  this.stylesheets.add("style/tab-pane.css")
  private var channelToStrategy : Map[String,FXLogStrategy] = Map.empty

  private def createStrategy(log : Log[_]) : FXLogStrategy = {
    GraphicsLogger.strategyType.get(log.channel) match {
      case Some(logType) => FXLogStrategy(logType)
      case _ => log match {
        case IntLog(_,_,_) => FXLogStrategy(GraphicsLogger.lineChart)
        case _ => FXLogStrategy(GraphicsLogger.textual)
      }
    }
  }
  override protected def processLog(log: Log[_]): Unit = {
    val strategy : FXLogStrategy = channelToStrategy.get(log.channel) match {
      case Some(s) => s
      case _ => {
        val res = createStrategy(log)
        res.init(log.channel)
        this.channelToStrategy += log.channel -> res
        res
      }
    }
    strategy.out(log)
  }
}
