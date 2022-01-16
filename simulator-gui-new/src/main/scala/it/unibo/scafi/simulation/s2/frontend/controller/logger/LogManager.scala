package it.unibo.scafi.simulation.s2.frontend.controller.logger

import java.util.Calendar

import it.unibo.utils.observer.Event
import it.unibo.utils.observer.Observer
import it.unibo.utils.observer.SimpleSource

import scala.util.Try

/**
 * a manager of logger observer, each message is send to all log observer, each one(looking the priority) decide to log
 * or not the message you can log information in this way:
 *
 * <pre> {@code LogManager.notify(IntLog("output","value",0) } </pre>
 */
object LogManager extends SimpleSource {
  override type O = LogObserver

  /**
   * a root class of LogManager observer
   */
  trait LogObserver extends Observer {
    // a list of channel accepted
    private var channelAccepted: ChannelList = acceptAll

    /**
     * allow to filter some channel il log observer
     * @param channel
     *   the channel list that describe what channel accept or not
     */
    def acceptChannel(channel: ChannelList): Unit = this.channelAccepted = channel

    final override def update(event: Event): Unit = event match {
      // verify if the log received can be showed
      case log: Log[_] =>
        channelAccepted match {
          case `acceptAll` => processLog(log)
          case acceptThese(set) if set.contains(log.channel) => processLog(log)
          case acceptExceptThese(set) if !set.contains(log.channel) => processLog(log)
          case _ =>
        }
    }

    /**
     * the strategy used by class that allow lo process and show log
     * @param log
     *   the log received
     */
    // TEMPLATE-METHOD
    protected def processLog(log: Log[_]): Unit
  }

  /**
   * root class of channel list used to describe a sequence of channel accepted
   */
  sealed trait ChannelList

  /**
   * accept all channel
   */
  case object acceptAll extends ChannelList

  /**
   * accept only a set of channel passed
   * @param channel
   *   the set of channel
   */
  case class acceptThese(channel: Set[String]) extends ChannelList

  /**
   * don't accept the channel specify in channel set
   * @param channel
   *   the set of channel
   */
  case class acceptExceptThese(channel: Set[String]) extends ChannelList
  /**
   * the root class of log event
   * @tparam V
   *   the value type
   */
  sealed trait Log[V] extends Event {
    val timestamp: Calendar = Calendar.getInstance()
    /**
     * @return
     *   the log channel
     */
    def channel: String

    /**
     * @return
     *   a label associated to value
     */
    def label: String

    /**
     * @return
     *   value logged
     */
    def value: V
  }

  /**
   * log a int value
   * @param channel
   *   log channel
   * @param label
   *   associated to value
   * @param value
   *   the int value to log
   */
  case class IntLog(channel: String, label: String, value: Int) extends Log[Int]

  /**
   * log a double value
   * @param channel
   *   log channel
   * @param label
   *   associated to value
   * @param value
   *   the double value to log
   */
  case class DoubleLog(channel: String, label: String, value: Double) extends Log[Double]

  /**
   * log a generic value
   * @param channel
   *   log channel
   * @param label
   *   associated to value
   * @param value
   *   the generic value to log
   */
  case class AnyLog(channel: String, label: String, value: Any) extends Log[Any]

  /**
   * log a string value
   * @param channel
   *   log channel
   * @param label
   *   associated to value
   * @param value
   *   the generic value to log
   */
  case class StringLog(channel: String, label: String, value: String) extends Log[String]

  case class TreeLog[NODE](channel: String, label: String, value: Seq[(Option[NODE], NODE, Any)])
      extends Log[Seq[(Option[NODE], NODE, Any)]]

  /**
   * a set of standard channel
   */
  object Channel {
    val Error = "error"
    val Std = "standard"
    val CommandResult = "command"
    val Export = "export"
    val SimulationRound = "simulation-round"
  }

  /**
   * a set of standard label
   */
  object Label {
    val Empty = ""
  }

  /**
   * a log observer that log value into file called as channel
   */
  object FileOutputObserver extends LogObserver {
    import it.unibo.scafi.simulation.s2.frontend.util.FileUtil._
    // verify if the outputDirectory is created or not
    Try(outputDirectory.createDirectory(force = true, failIfExists = true))
    // home path used to create file
    implicit val home: String = outputDirectory.path
    // the set of channel logged, used to create the right number of file
    private var channel: Set[String] = Set.empty
    override protected def processLog(log: Log[_]): Unit = if (channel.contains(log.channel)) {
      // if the channel is already logged, append log value
      append(path(log.channel), log.label + " = " + log.value.toString)
    } else {
      // if the channel is already no logged, overwrite file with new value
      write(path(log.channel), log.label + " = " + log.value.toString)
      channel += log.channel
    }
  }

  /**
   * a console observer that log value into standard output
   */
  class ConsoleTimestampOutputObserver extends LogObserver {
    override protected def processLog(log: Log[_]): Unit =
      println(s" ${log.timestamp.toInstant.toString} ${if (log.channel.isEmpty) "" else log.channel + " "}${log.value}")
  }

  class ConsoleOutputObserver extends LogObserver {
    override protected def processLog(log: Log[_]): Unit = println(log.value)
  }
}
