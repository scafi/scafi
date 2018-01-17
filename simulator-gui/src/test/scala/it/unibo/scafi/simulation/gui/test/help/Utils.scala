package it.unibo.scafi.simulation.gui.test.help

object Utils {
  def timeTest(maxT : Float)(f : => Unit) : Float = {
    val t0 = System.currentTimeMillis()
    f
    val t1 = System.currentTimeMillis()
    assert(maxT > ((t1 - t0) / 1000))
    return (t1 - t0) / 1000.0f
  }
}
