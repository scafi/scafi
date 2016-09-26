package examples

import it.unibo.scafi.incarnations.BasicAbstractSpatialSimulationIncarnation
import it.unibo.scafi.space.Point2D

object BasicSpatialIncarnation extends BasicAbstractSpatialSimulationIncarnation {
  override type P = Point2D

  trait MyEuclideanStrategy extends EuclideanStrategy {
    this: Basic3DSpace[_] =>
    override val proximityThreshold = 1.8
  }

  override def buildNewSpace[E](elems: Iterable[(E,P)]): SPACE[E] =
    new Basic3DSpace(elems.toMap) with MyEuclideanStrategy
}

import BasicSpatialIncarnation._

object DemoSpatialLauncher extends App {
  object DemoSpatial extends AggregateProgram {
    def mySensor():Int = sense[Int]("sensor")
    def gradient(source: Boolean): Double =
      rep(Double.MaxValue){
        distance => mux(source) { 0.0 } {
          foldhood(Double.MaxValue)((x,y)=>if (x<y) x else y)(nbr{distance}+nbrvar[Double](NBR_RANGE_NAME))}}
    def main() = foldhood(0)(_+_){1} //gradient(mySensor()==1)
  }

  val net = simulatorFactory.gridLike(
    n = 3,
    m = 3,
    stepx = 1,
    stepy = 1,
    eps = 0,
    rng = 1.2)

  net.addSensor(name = "sensor", value = 0)
  net.chgSensorValue(name = "sensor", ids = Set(1), value = 1)
  net.addSensor(name = "sensor2", value = 0)
  net.chgSensorValue(name = "sensor2", ids = Set(98), value = 1)
  net.addSensor(name = "obstacle", value = false)
  net.chgSensorValue(name = "obstacle", ids = Set(44,45,46,54,55,56,64,65,66), value = true)
  net.addSensor(name = "label", value = "no")
  net.chgSensorValue(name = "label", ids = Set(1), value = "go")

  var v = java.lang.System.currentTimeMillis()

  net.executeMany(
    node = DemoSpatial,
    size = 100000,
    action = (n,i) => {
      if (i % 1000 == 0) {
        println(net)
        val newv = java.lang.System.currentTimeMillis()
        println(newv-v)
        v=newv
      }
    })
}
