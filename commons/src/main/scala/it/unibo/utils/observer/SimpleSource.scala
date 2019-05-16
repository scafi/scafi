package it.unibo.utils.observer

/**
  * the order of notification to observer isn't preserved
  */
trait SimpleSource extends Source {
  private var _observers = Set[O]()

  override def attach(observer : O): Boolean =
    if(_observers contains observer){ false }
    else {
      _observers += observer
      true
    }

  /**
    * remove an observer to the source
    * @param observer want to stop observer this source
    * @return false if the observer currently doesn't observer the source true otherwise
    */
  def detach(observer: O) : Boolean =
    if (!(_observers contains observer)){ false}
    else{
      _observers -= observer
     true
    }

  protected def observers: Iterable[O] = _observers
}
