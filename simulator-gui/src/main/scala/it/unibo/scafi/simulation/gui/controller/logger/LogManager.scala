package it.unibo.scafi.simulation.gui.controller.logger

import java.io.{File, FileWriter, PrintWriter}

import it.unibo.scafi.simulation.gui.pattern.observer.{Event, Observer, SimpleSource}

import scala.io.Source
import scala.util.Try

/**
  * a manager of logger observer, each message is send to all
  * log observer, each one(looking the priority) decide to log or
  * not the message
  */
object LogManager extends SimpleSource {

  override type O = LogObserver

  trait LogObserver extends Observer

  sealed trait Log[V] extends Event{
    def channel : String
    def label : String
    def value : V
  }
  case class IntLog(channel: String, label: String, value : Int) extends Log[Int]
  case class DoubleLog(channel: String, label: String, value : Double) extends Log[Double]
  case class AnyLog(channel: String, label: String, value : Any) extends Log[Any]
  case class StringLog(channel: String, label: String, value : String) extends Log[String]

  object Channel {
    val Error = "error"
    val Std = "standard"
    val commandResult = "command"
  }

  object FileObserver extends LogObserver {
    import it.unibo.scafi.simulation.gui.util.FileUtil._
    Try{outputDirectory.createDirectory(true,true)}
    implicit val home = outputDirectory.path
    private var channel : Set[String] = Set.empty
    override def update(event: Event): Unit = event match {
      case log : Log[_] => if(channel.contains(log.channel)) {
        append(path(log.channel),log.value.toString)
      } else {
        write(path(log.channel),log.value.toString)
        channel += log.channel
      }
    }
  }

  this.attach(FileObserver)
}
