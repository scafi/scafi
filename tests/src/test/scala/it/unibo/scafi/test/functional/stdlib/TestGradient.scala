package it.unibo.scafi.test.functional.stdlib

import it.unibo.scafi.test.FunctionalTestIncarnation._
import it.unibo.scafi.test.functional.{ScafiAssertions, ScafiTestUtils}
import org.scalatest._

class TestGradient extends FunSpec with BeforeAndAfterAll {
  import ScafiAssertions._
  import ScafiTestUtils._

  var stdNet: Network with SimulatorOps = standardNetwork()
  val infinity: Double = Double.PositiveInfinity

  override protected def beforeAll(): Unit = {
    stdNet = standardNetwork()
    stdNet.addSensor(name = "source", value = false)
  }

  private[this] trait TestProgram extends AggregateProgram with StandardSensors with Gradients

  describe("Classic Gradient") {
    describe("On the standard network") {
      it("Should be possible to build a gradient of distances on node 0") {
        stdNet.chgSensorValue("source", Set(0), true)
        exec(new TestProgram {
          override def main(): Double = classicGradient(sense("source"))
        }, ntimes = fewRounds)(stdNet)

        assertNetworkValues((0 to 8).zip(List(
          0.0, 1.0, 2.0,
          1.0, 1.41, 2.41,
          2.0, 2.42, infinity
        )).toMap, Some((d1:Double, d2:Double) => d1===d2 +- 0.01))(stdNet)
      }
      it("Should be possible to build a gradient of distances on node 4") {
        stdNet.chgSensorValue("source", Set(4), true)
        exec(new TestProgram {
          override def main(): Double = classicGradient(sense("source"))
        }, ntimes = fewRounds)(stdNet)

        assertNetworkValues((0 to 8).zip(List(
          1.41, 1.0, 1.41,
          1.0, 0.0, 1.0,
          1.41, 1.0, infinity
        )).toMap, Some((d1:Double, d2:Double) => d1===d2 +- 0.01))(stdNet)
      }
      it("Should return a constant field if no source is selected") {
        exec(new TestProgram {
          override def main(): Double = classicGradient(sense("source"))
        }, ntimes = fewRounds)(stdNet)

        assertNetworkValues((0 to 8).zip(List(
          infinity, infinity, infinity,
          infinity, infinity, infinity,
          infinity, infinity, infinity
        )).toMap, Some((d1:Double, d2:Double) => d1===d2 +- 0.01))(stdNet)
      }
      it("Should build a gradient for each source, node 0 and 4"){
        stdNet.chgSensorValue("source", Set(0, 4), true)
        exec(new TestProgram {
          override def main(): Double = classicGradient(sense("source"))
        }, ntimes = someRounds)(stdNet)

        assertNetworkValues((0 to 8).zip(List(
          0.0, 1.0, 1.41,
          1.0, 0.0, 1.0,
          1.41, 1.0, infinity
        )).toMap, Some((d1:Double, d2:Double) => d1===d2 +- 0.01))(stdNet)
      }
    }
  }
}
