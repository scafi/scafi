package it.unibo.scafi.simulation.gui.test.controller

import it.unibo.scafi.simulation.gui.controller.logger.LogManager
import org.scalatest.{FunSpec, Matchers}
class TestLogManager extends FunSpec with Matchers{
  val checkThat = new ItWord
  import LogManager._
  val lowPriority = new TestableLogObserver(Set(Low))
  val allPriority = new TestableLogObserver(Set(Low,Middle,High))

  LogManager <-- lowPriority <-- allPriority

  checkThat("each observer must be notify with low priority ") {
    log("Message",Low)
    assert(lowPriority.getLoggingCount!= 0)
    assert(allPriority.getLoggingCount != 0)
  }

  checkThat("an observer with low visibility can see middle priority messages") {
    log("Another messager",High)
    assert(lowPriority.getLoggingCount == 1)
    assert(allPriority.getLoggingCount > 1)
  }

}
class TestableLogObserver[U <: LogManager.Priority] (priority : Set[U]) extends LogManager.LogObserver {
  private var loggingCount = 0
  def getLoggingCount = loggingCount

  override protected def logging(message: String, priority: LogManager.Priority): Unit = {
    if(this.priority.find(_ == priority).isDefined) {
      loggingCount += 1
    }
  }
}

