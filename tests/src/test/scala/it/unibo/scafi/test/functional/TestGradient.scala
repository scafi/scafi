package it.unibo.scafi.test.functional

/**
 * Created by: Roberto Casadei
 * Created on date: 31/10/15
 */

import it.unibo.scafi.test.FunctionalTestIncarnation._
import org.scalatest._

class TestGradient extends FlatSpec with Matchers {
  import ScafiAssertions._
  import ScafiTestUtils._

  val stepx: Double = 7.0
  val stepy: Double = 10.0

  private[this] trait SimulationContextFixture {
    val net: Network with SimulatorOps =
      SetupNetwork(simulatorFactory.gridLike(n = 3, m = 3, stepx = stepx, stepy = stepy, eps = 0, rng = 11))
    implicit val node = new Node
  }

  private[this] class Node extends AggregateProgram {
    override type MainResult = Any
    override def main() = ???

    def mySensor():Int = sense[Int]("sensor")

    def hopGradient(source: Boolean): Int = {
      rep(10){
        hops => { mux(source){ 0 } {
          1+minHood[Int](nbr[Int]{ hops }) } }
      }
    }

    def gradient(source: Boolean): Double =
      rep(Double.MaxValue){
        distance => mux(source) { 0.0 } {
          foldhood(Double.MaxValue)((x,y)=>if (x<y) x else y)(nbr{distance}+nbrvar[Double](NBR_RANGE_NAME))
        }
      }
  }

  def SetupNetwork(n: Network with SimulatorOps) = {
    n.addSensor(name = "sensor", value = 0)
    n.chgSensorValue(name = "sensor", ids = Set(8), value = 1)
    n
  }

  it should "be possible to build a gradient of hops (steps)" in new SimulationContextFixture {
    // ARRANGE
    import node._
    // ACT
    implicit val endNet = runProgram { hopGradient(mySensor()==1) } (net)
    // ASSERT
    assertNetworkValues((0 to 8).zip(List(
      4, 3, 2,
      3, 2, 1,
      2, 1, 0
    )).toMap)
  }

  it should "be possible to build a gradient of distances" in new SimulationContextFixture {
    // ARRANGE
    import node._
    // ACT
    implicit val endNet = runProgram { gradient(mySensor()==1) } (net)
    // ASSERT
    assertNetworkValues((0 to 8).zip(List[Double](
      34, 27, 20,
      24, 17, 10,
      14, 7,  0
    )).toMap, Some( (d1:Double, d2:Double) => d1===d2 +- 0.0002 ))
  }

}