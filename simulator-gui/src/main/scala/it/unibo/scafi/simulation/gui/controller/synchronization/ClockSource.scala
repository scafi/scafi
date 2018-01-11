package it.unibo.scafi.simulation.gui.controller.synchronization

import scala.collection.concurrent.TrieMap

trait ClockSource {
  /**the priority associated to a priority*/
  sealed trait Priority
  object High extends Priority
  object Medium extends Priority
  object Low extends Priority
  //thread-safe
  private var observers : TrieMap[Priority, Set[ClockObserver]] = TrieMap.apply(High->Set(),Medium->Set(),Low->Set())

  val dt : Int = _

  private val clock : Thread = new Thread() {
    override def run(): Unit = {
      update(High)
      update(Medium)
      update(Low)
    }
    private def update(p : Priority) = observers(p) foreach { _.tick() }

  }
  def subscribeObserver(observer : ClockObserver,priority: Priority) = {
    var observers = this.observers(priority)
    observers +=  observer
    this.observers += priority -> observers
  }

  def unsubcribeObserver(observer: ClockObserver, priority: Priority) = {
    var observers = this.observers(priority)
    observers -=  observer
    this.observers += priority -> observers
  }
}
//an observer of clock event

trait ClockObserver {
  def tick()
}


