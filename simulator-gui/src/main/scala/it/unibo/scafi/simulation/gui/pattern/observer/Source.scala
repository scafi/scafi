package it.unibo.scafi.simulation.gui.pattern.observer
//OBSERVER PATTERN
/**
  * define a generic event source
  */
trait Source {
  /**
    * observer type, a source can notify a observer class
    */
  type O <: Observer
  //TEMPLATE METHOD
  /**
    * @return the observers attached to current source
    */
  protected def observers: Iterable[O]

  /**
    * fast way to add an observer
    * @param observer want to observe current source
    * @return the source
    */
  final def <-- (observer : O): this.type = {
    attach(observer)
    return this
  }


  final def <--! (observer : O): this.type =  {
    detach(observer)
    return this
  }

  /**
    * add an observer to the source
    * @param observer want observe this source
    * @return false if the observer currently observe the source true otherwise
    */
  def attach(observer : O): Boolean
  /**
    * remove an observer to the source
    * @param observer want to stop observer this source
    * @return false if the observer currently doesn't observer the source true otherwise
    */
  def detach(observer: O) : Boolean

  /**
    * notify all the observer
    * @param e the event generate
    */
  def notify(e :Event) =  observers foreach (_ update e)
}

/**
  * the root interface of all event
  */
trait Event

trait Observer {
  private var _events : List[Event] = List[Event]()
  /**
    * store the event received
    * @param event the event produced
    */
  def update(event: Event) : Unit = _events = event :: _events

  /**
    * @return the sequence of event listened and clear the current queue of event
    */
  def events : Seq[Event] = {
    val res = this._events
    this._events = List[Event]()
    res
  }
  /**
    * clear the queue of events
    */
  def clear() : Unit = events
}
