package it.unibo.scafi.simulation.frontend.view.scalaFX.logger

import javafx.scene.chart.XYChart

import it.unibo.scafi.simulation.frontend.controller.logger.LogManager.{DoubleLog, IntLog, Log, TreeLog}
import it.unibo.scafi.simulation.frontend.view.GraphicsLogger
import it.unibo.scafi.simulation.frontend.view.GraphicsLogger.{LogType, textual}

import scalafx.Includes._
import scalafx.application.Platform
import scalafx.scene.chart.{Axis, LineChart, NumberAxis}
import scalafx.scene.control._
import scalafx.scene.layout.StackPane
/**
  * a strategy used to display log information
  */
trait FXLogStrategy {
  /**
    * init the log strategy
    * @param channel the log channel
    */
  def init(channel : String)

  /**
    * @param log out the log passed
    */
  def out(log : Log[_])
}

/**
  * a set of fx log strategy
  */
object FXLogStrategy {
  /**
    * simple factory, create a log strategy
    * with log type passed
    * @param logType the log type passed
    * @return log strategy
    */
  def apply(logType : LogType) : FXLogStrategy = logType match {
    case `textual` => new TextOutput
    case GraphicsLogger.lineChart => new LineChartOutput
    case GraphicsLogger.treeView => new TreeOutput
  }

  /**
    * a line chart output, show numeric value in a line chart
    */
  private class LineChartOutput extends FXLogStrategy {
    var i = 0
    private val maxElement = 50
    val numberX : Axis[Number] = new NumberAxis
    numberX.tickLabelsVisible = false
    val numberY : Axis[Number] = new NumberAxis
    val char = new LineChart[Number,Number](numberX,numberY)
    val series = new XYChart.Series[Number,Number]
    char.animated = false
    char.verticalGridLinesVisible = false
    char.horizontalGridLinesVisible = false
    char.createSymbols = false
    private val pane = new StackPane {
      children = char
    }
    pane.style = "-fx-background-color: white;-fx-border-color: black"
    override def init(channel : String): Unit = {
      val tab = new Tab {
        text = channel
        closable = false
        content = pane
      }
      Platform.runLater{FXLogger.tabs.add(tab)}
    }

    override def out(log: Log[_]): Unit = {
      def outInChar(name : String, value : Number) : Unit = Platform.runLater {
        if(series.getData.size > maxElement) {
          series.getData.remove(0)
          series.dataProperty().value.foreach(x => x.setXValue(x.getXValue.intValue() - 1))
        } else {
          i+=1
        }

        series.name = name
        series.getData.add(new XYChart.Data(i,value))
        char.data = series
        char.verticalZeroLineVisible = false
      }
      log match {
        case IntLog(_,name,value)  => outInChar(name,value)
        case DoubleLog(_,name,value) => outInChar(name,value)
        case _ =>
      }
    }
  }

  /**
    * show log in a textual form
    */
  private class TextOutput extends FXLogStrategy {
    private val maxChar = 10000
    private val area = new TextArea
    area.editable = false
    private val pane = new StackPane {
      children = area
    }

    pane.stylesheets.add("style/console-like.css")
    override def init(channel : String): Unit = {
      val tab = new Tab {
        text = channel
        closable = false
        content = pane
      }
      Platform.runLater{FXLogger.tabs.add(tab)}
    }

    override def out(log: Log[_]): Unit = {
      Platform.runLater {
        val size = area.text.value.length
        val currentText = area.text.value
        val label = if(log.label.isEmpty) "" else log.label + " "
        area.text = label + log.value + "\n" + currentText.substring(0, if(size > maxChar) maxChar else size)
      }
    }
  }

  /**
    * show log like a tree
    */
  private class TreeOutput extends FXLogStrategy {
    var mappedTree : Map[String, TreeItem[String]] = Map.empty
    val tree = new TreeView[String]
    val rootElem = new TreeItem[String]
    tree.stylesheets.add("style/console-like.css")
    override def init(channel: String): Unit = {
      val tab = new Tab {
        text = channel
        closable = false
        content = tree
      }
      Platform.runLater {
        tree.root = rootElem
        rootElem.value = channel
        FXLogger.tabs.add(tab)
      }
    }
    /**
      *
      * @param log out the log passed
      */
    override def out(log: Log[_]): Unit = {
      var map : Map[Any,TreeItem[String]] = Map.empty
      log match {
        case TreeLog(_,name,list) =>
          Platform.runLater {
            val logRoot = mappedTree.get(name) match {
              case Some(tree) => tree
              case _ => val tree = new TreeItem[String]
                rootElem.children += tree
                mappedTree += name -> tree
                tree

            }

            logRoot.value = name + " " + list.head._2.toString + " "  + list.head._3.toString
            logRoot.children.clear()
            map += list.head._2 -> logRoot
            list.tail foreach { x => {
              val elem = new TreeItem[String](x._2.toString.substring(x._1.get.toString.size) + " " + x._3.toString)
              map(x._1.get).children.add(elem)
              elem.expanded = true
              map += x._2 -> elem
            }}
          }

        case _ =>
      }
    }
  }

}