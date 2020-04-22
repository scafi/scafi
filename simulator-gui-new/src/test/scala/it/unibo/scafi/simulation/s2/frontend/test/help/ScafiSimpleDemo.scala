package it.unibo.scafi.simulation.s2.frontend.test.help
import it.unibo.scafi.incarnations.BasicSimulationIncarnation.AggregateProgram
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.reflection.Demo
@Demo
class ScafiSimpleDemo extends AggregateProgram {
  override def main() = rep(0)(_ + 1) // the aggregate program to run
}