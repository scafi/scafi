package it.unibo.scafi.incarnations

/**
 * @author Roberto Casadei
 *
 */

trait BasicPrograms { incarnation: BasicAbstractIncarnation =>

  class NeighbourCounting extends AggregateProgram {
    override def main(): Any = foldhood(0)(_+_){1}
  }

  class ConstantField(val value: Any) extends AggregateProgram {
    override def main(): Any = value
  }

  class HopGradient(val srcSensor: LSNS) extends AggregateProgram {
    def hopGradient(source: Boolean): Int = {
      rep(10000){
        hops => { mux(source) { 0 } { 1+minHood[Int](nbr[Int]{ hops }) } }
      }
    }

    override def main(): Any = hopGradient(sense(srcSensor))
  }

  class Gradient(val srcSensor: LSNS) extends AggregateProgram {
    def gradient(source: Boolean): Double =
      rep(Double.MaxValue){
        distance => mux(source) { 0.0 } {
          foldhood(Double.MaxValue)((x,y)=>if (x<y) x else y)(nbr{distance}+nbrvar[Double](NBR_RANGE_NAME))}}

    override def main(): Any = gradient(sense(srcSensor))
  }

}
