package examples

import it.unibo.scafi.config.GridSettings
import it.unibo.scafi.incarnations.BasicAbstractSpatialSimulationIncarnation
import it.unibo.scafi.space.{Point2D, SpaceHelper}

object BasicSpatialIncarnation extends BasicAbstractSpatialSimulationIncarnation {
  override type P = Point2D

  trait MyEuclideanStrategy extends EuclideanStrategy {
    this: Basic3DSpace[_] =>
    override val proximityThreshold = 0.1
  }

  override def buildNewSpace[E](elems: Iterable[(E,P)]): SPACE[E] =
    new Basic3DSpace(elems.toMap) with MyEuclideanStrategy
}

import BasicSpatialIncarnation._

object DemoSpatialLauncher extends App {
  object DemoSpatial extends AggregateProgram {
    def mySensor():Int = sense[Int]("sensor")
    def gradient(source: Boolean): Double = rep(Double.MaxValue){
      distance => mux(source) { 0.0 } {
        minHoodPlus { nbr{distance}+nbrvar[Double](NBR_RANGE_NAME) }
      }
    }
    def main() = foldhood(0)(_+_){1} //gradient(mySensor()==1)
  }

  val (ncols,nrows) = (3,3)
  val (stepx,stepy) = (1,1)
  val positions = SpaceHelper.GridLocations(GridSettings(nrows,ncols,stepx,stepy,tolerance=0))
  val ids = for(i <- 1 to ncols*nrows) yield i
  val devsToPos = ids.zip(positions).toMap
  val net = new SpaceAwareSimulator(
    space = new Basic3DSpace(devsToPos, proximityThreshold = 1.8),
    devs = devsToPos.map { case (d, p) => d -> new DevInfo(d, p,
      lsns => if (lsns == "sensor" && d == 3) 1 else 0,
      nsns => nbr => null)
      }
    )

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
      if(i>0 && i % 50000 == 0){
        //net.chgSensorValue("sensor", Set(3), 0)
        net.setPosition(3, new Point2D(0,0))
      }
    })
}
