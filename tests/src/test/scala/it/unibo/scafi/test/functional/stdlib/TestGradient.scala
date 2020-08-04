package it.unibo.scafi.test.functional.stdlib

import it.unibo.scafi.test.FunctionalTestIncarnation._
import it.unibo.scafi.test.functional.{ScafiAssertions, ScafiTestUtils}
import org.scalatest._

class TestGradient extends FunSpec with BeforeAndAfterEach {
  import ScafiAssertions._
  import ScafiTestUtils._

  var stdNet: Network with SimulatorOps = standardNetwork()
  val infinity: Double = Double.PositiveInfinity

  override protected def beforeEach(): Unit = {
    stdNet = standardNetwork()
    stdNet.addSensor(name = "source", value = false)
  }

  private[this] trait TestProgram extends AggregateProgram with StandardSensors with BlockG with StateManagement with Gradients with GenericUtils

  def sameAsClassic(prog: () => Double,
                    net: Network with SimulatorOps,
                    sourceSensor: String = "source",
                    rounds: Int = manyRounds,
                    tolerance: Double = 0.01
                   ): Boolean = {
    exec(new TestProgram {
      override def main(): (Double, Double) = (prog(), classicGradient(sense(sourceSensor)))
    })(net)

    //le coppie devono essere uguali
    println(net.valueMap())

    true
  }

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
      it("Should be able to react to a change of source"){
        stdNet.chgSensorValue("source", Set(0), true)
        exec(new TestProgram {
          override def main(): Double = classicGradient(sense("source"))
        }, ntimes = fewRounds)(stdNet)

        stdNet.chgSensorValue("source", Set(0), false)
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
    }
  }
  describe("Hop Gradient"){
    describe("On Standard Network"){
      it("Should be possible to build a gradient of distances on node 0") {
        stdNet.chgSensorValue("source", Set(0), true)
        exec(new TestProgram {
          override def main(): Double = hopGradient(sense("source"))
        }, ntimes = fewRounds)(stdNet)

        assertNetworkValues((0 to 8).zip(List(
          0.0, 1.0, 2.0,
          1.0, 1.0, 2.0,
          2.0, 2.0, infinity
        )).toMap)(stdNet)
      }
      it("Should be possible to build a gradient of distances on node 4") {
        stdNet.chgSensorValue("source", Set(4), true)
        exec(new TestProgram {
          override def main(): Double = hopGradient(sense("source"))
        }, ntimes = fewRounds)(stdNet)

        assertNetworkValues((0 to 8).zip(List(
          1.0, 1.0, 1.0,
          1.0, 0.0, 1.0,
          1.0, 1.0, infinity
        )).toMap)(stdNet)
      }
      it("Should return a constant field if no source is selected") {
        exec(new TestProgram {
          override def main(): Double = hopGradient(sense("source"))
        }, ntimes = fewRounds)(stdNet)

        assertNetworkValues((0 to 8).zip(List(
          infinity, infinity, infinity,
          infinity, infinity, infinity,
          infinity, infinity, infinity
        )).toMap)(stdNet)
      }
      it("Should build a gradient for each source, node 0 and 4"){
        stdNet.chgSensorValue("source", Set(0, 4), true)
        exec(new TestProgram {
          override def main(): Double = hopGradient(sense("source"))
        }, ntimes = someRounds)(stdNet)

        assertNetworkValues((0 to 8).zip(List(
          0.0, 1.0, 1.0,
          1.0, 0.0, 1.0,
          1.0, 1.0, infinity
        )).toMap)(stdNet)
      }
      it("Should be able to react to a change of source"){
        stdNet.chgSensorValue("source", Set(0), true)
        exec(new TestProgram {
          override def main(): Double = hopGradient(sense("source"))
        }, ntimes = fewRounds)(stdNet)

        stdNet.chgSensorValue("source", Set(0), false)
        stdNet.chgSensorValue("source", Set(4), true)
        exec(new TestProgram {
          override def main(): Double = hopGradient(sense("source"))
        }, ntimes = fewRounds)(stdNet)

        assertNetworkValues((0 to 8).zip(List(
          1.0, 1.0, 1.0,
          1.0, 0.0, 1.0,
          1.0, 1.0, infinity
        )).toMap, Some((d1:Double, d2:Double) => d1===d2 +- 0.01))(stdNet)
      }
    }
  }
  describe("BIS Gradient"){
    describe("On the standard network") {
      it("Should be possible to build a gradient of distances on node 0") {
        stdNet.chgSensorValue("source", Set(0), true)
        exec(new TestProgram {
          override def main(): Double = bisGradient(sense("source"))
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
          override def main(): Double = bisGradient(sense("source"))
        }, ntimes = fewRounds)(stdNet)

        assertNetworkValues((0 to 8).zip(List(
          1.41, 1.0, 1.41,
          1.0, 0.0, 1.0,
          1.41, 1.0, infinity
        )).toMap, Some((d1:Double, d2:Double) => d1===d2 +- 0.01))(stdNet)
      }
      it("Should return a constant field if no source is selected") {
        exec(new TestProgram {
          override def main(): Double = bisGradient(sense("source"))
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
          override def main(): Double = bisGradient(sense("source"))
        }, ntimes = someRounds)(stdNet)

        assertNetworkValues((0 to 8).zip(List(
          0.0, 1.0, 1.41,
          1.0, 0.0, 1.0,
          1.41, 1.0, infinity
        )).toMap, Some((d1:Double, d2:Double) => d1===d2 +- 0.01))(stdNet)
      }
      it("Should be able to react to a change of source"){
        stdNet.chgSensorValue("source", Set(0), true)
        exec(new TestProgram {
          override def main(): Double = bisGradient(sense("source"))
        }, ntimes = fewRounds)(stdNet)

        stdNet.chgSensorValue("source", Set(0), false)
        stdNet.chgSensorValue("source", Set(4), true)
        exec(new TestProgram {
          override def main(): Double = bisGradient(sense("source"))
        }, ntimes = fewRounds)(stdNet)

        assertNetworkValues((0 to 8).zip(List(
          1.41, 1.0, 1.41,
          1.0, 0.0, 1.0,
          1.41, 1.0, infinity
        )).toMap, Some((d1:Double, d2:Double) => d1===d2 +- 0.01))(stdNet)
      }
    }
  }
  describe("CRF Gradient"){
    describe("On the standard network") {
      it("Should be possible to build a gradient of distances on node 0") {
        stdNet.chgSensorValue("source", Set(0), true)
        exec(new TestProgram {
          override def main(): Double = crfGradient(sense("source"))
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
          override def main(): Double = crfGradient(sense("source"))
        }, ntimes = fewRounds)(stdNet)

        assertNetworkValues((0 to 8).zip(List(
          1.41, 1.0, 1.41,
          1.0, 0.0, 1.0,
          1.41, 1.0, infinity
        )).toMap, Some((d1:Double, d2:Double) => d1===d2 +- 0.01))(stdNet)
      }
      it("Should return a constant field if no source is selected") {
        exec(new TestProgram {
          override def main(): Double = crfGradient(sense("source"))
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
          override def main(): Double = crfGradient(sense("source"))
        }, ntimes = someRounds)(stdNet)

        assertNetworkValues((0 to 8).zip(List(
          0.0, 1.0, 1.41,
          1.0, 0.0, 1.0,
          1.41, 1.0, infinity
        )).toMap, Some((d1:Double, d2:Double) => d1===d2 +- 0.01))(stdNet)
      }
      it("Should be able to react to a change of source"){
        stdNet.chgSensorValue("source", Set(0), true)
        exec(new TestProgram {
          override def main(): Double = crfGradient(sense("source"))
        }, ntimes = manyRounds)(stdNet)

        stdNet.chgSensorValue("source", Set(0), false)
        stdNet.chgSensorValue("source", Set(4), true)
        //Note: requires more iterations than the the others
        exec(new TestProgram {
          override def main(): Double = crfGradient(sense("source"))
        }, ntimes = manyManyRounds * 10)(stdNet)

        assertNetworkValues((0 to 8).zip(List(
          1.41, 1.0, 1.41,
          1.0, 0.0, 1.0,
          1.41, 1.0, infinity
        )).toMap, Some((d1:Double, d2:Double) => d1===d2 +- 0.01))(stdNet)
      }
    }
  }
  describe("Flex Gradient"){
    describe("On the standard network") {
      it("Should be possible to build a gradient of distances on node 0") {
        stdNet.chgSensorValue("source", Set(0), true)
        exec(new TestProgram {
          override def main(): Double = flexGradient(sense("source"), metric = nbrRange, epsilon = 0.05)
        }, ntimes = manyManyRounds)(stdNet)

        assertNetworkValues((0 to 8).zip(List(
          0.0, 1.0, 2.0,
          1.0, 1.41, 2.41,
          2.0, 2.42, infinity
        )).toMap, Some((d1:Double, d2:Double) => d1===d2 +- 0.1))(stdNet)
      }
      it("Should be possible to build a gradient of distances on node 4") {
        stdNet.chgSensorValue("source", Set(4), true)
        exec(new TestProgram {
          override def main(): Double = flexGradient(sense("source"), metric = nbrRange, epsilon = 0.05)
        }, ntimes = fewRounds)(stdNet)

        assertNetworkValues((0 to 8).zip(List(
          1.41, 1.0, 1.41,
          1.0, 0.0, 1.0,
          1.41, 1.0, infinity
        )).toMap, Some((d1:Double, d2:Double) => d1===d2 +- 0.1))(stdNet)
      }
      it("Should return a constant field if no source is selected") {
        exec(new TestProgram {
          override def main(): Double = flexGradient(sense("source"), metric = nbrRange, epsilon = 0.05)
        }, ntimes = fewRounds)(stdNet)

        assertNetworkValues((0 to 8).zip(List(
          infinity, infinity, infinity,
          infinity, infinity, infinity,
          infinity, infinity, infinity
        )).toMap, Some((d1:Double, d2:Double) => d1===d2 +- 0.1))(stdNet)
      }
      it("Should build a gradient for each source, node 0 and 4"){
        stdNet.chgSensorValue("source", Set(0, 4), true)
        exec(new TestProgram {
          override def main(): Double = flexGradient(sense("source"), metric = nbrRange, epsilon = 0.05)
        }, ntimes = manyRounds)(stdNet)
        assertNetworkValues((0 to 8).zip(List(
          0.0, 1.0, 1.41,
          1.0, 0.0, 1.0,
          1.41, 1.0, infinity
        )).toMap, Some((d1:Double, d2:Double) => d1===d2 +- 0.1))(stdNet)
      }
      it("Should be able to react to a change of source"){
        stdNet.chgSensorValue("source", Set(0), true)
        exec(new TestProgram {
          override def main(): Double = flexGradient(sense("source"), metric = nbrRange, epsilon = 0.05)
        }, ntimes = someRounds)(stdNet)

        stdNet.chgSensorValue("source", Set(0), false)
        stdNet.chgSensorValue("source", Set(4), true)
        //Note: requires more iterations than the the others
        exec(new TestProgram {
          override def main(): Double = flexGradient(sense("source"), metric = nbrRange, epsilon = 0.05)
        }, ntimes = someRounds)(stdNet)

        assertNetworkValues((0 to 8).zip(List(
          1.41, 1.0, 1.41,
          1.0, 0.0, 1.0,
          1.41, 1.0, infinity
        )).toMap, Some((d1:Double, d2:Double) => d1===d2 +- 0.1))(stdNet)
      }
    }
  }
  describe("SVD Gradient"){
    describe("On the standard network") {
      it("Should be possible to build a gradient of distances on node 0") {
        stdNet.chgSensorValue("source", Set(0), true)
        exec(new TestProgram {
          override def main(): Double = svdGradient(sense("source"))
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
          override def main(): Double = svdGradient(sense("source"))
        }, ntimes = fewRounds)(stdNet)

        assertNetworkValues((0 to 8).zip(List(
          1.41, 1.0, 1.41,
          1.0, 0.0, 1.0,
          1.41, 1.0, infinity
        )).toMap, Some((d1:Double, d2:Double) => d1===d2 +- 0.01))(stdNet)
      }
      it("Should return a constant field if no source is selected") {
        exec(new TestProgram {
          override def main(): Double = svdGradient(sense("source"))
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
          override def main(): Double = svdGradient(sense("source"))
        }, ntimes = someRounds)(stdNet)

        assertNetworkValues((0 to 8).zip(List(
          0.0, 1.0, 1.41,
          1.0, 0.0, 1.0,
          1.41, 1.0, infinity
        )).toMap, Some((d1:Double, d2:Double) => d1===d2 +- 0.01))(stdNet)
      }
      it("Should be able to react to a change of source"){
        stdNet.chgSensorValue("source", Set(0), true)
        exec(new TestProgram {
          override def main(): Double = svdGradient(sense("source"))
        }, ntimes = fewRounds)(stdNet)

        stdNet.chgSensorValue("source", Set(0), false)
        stdNet.chgSensorValue("source", Set(4), true)
        //Note: requires more iterations than the the others
        exec(new TestProgram {
          override def main(): Double = svdGradient(sense("source"))
        }, ntimes = manyRounds)(stdNet)

        assertNetworkValues((0 to 8).zip(List(
          1.41, 1.0, 1.41,
          1.0, 0.0, 1.0,
          1.41, 1.0, infinity
        )).toMap, Some((d1:Double, d2:Double) => d1===d2 +- 0.01))(stdNet)
      }
    }
  }
  describe("ULT Gradient"){
    describe("On the standard network") {
      it("Should be possible to build a gradient of distances on node 0") {
        stdNet.chgSensorValue("source", Set(0), true)
        exec(new TestProgram {
          override def main(): Double = ultGradient(sense("source"))
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
          override def main(): Double = ultGradient(sense("source"))
        }, ntimes = fewRounds)(stdNet)

        assertNetworkValues((0 to 8).zip(List(
          1.41, 1.0, 1.41,
          1.0, 0.0, 1.0,
          1.41, 1.0, infinity
        )).toMap, Some((d1:Double, d2:Double) => d1===d2 +- 0.01))(stdNet)
      }
      it("Should return a constant field if no source is selected") {
        exec(new TestProgram {
          override def main(): Double = ultGradient(sense("source"))
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
          override def main(): Double = ultGradient(sense("source"))
        }, ntimes = someRounds)(stdNet)

        assertNetworkValues((0 to 8).zip(List(
          0.0, 1.0, 1.41,
          1.0, 0.0, 1.0,
          1.41, 1.0, infinity
        )).toMap, Some((d1:Double, d2:Double) => d1===d2 +- 0.01))(stdNet)
      }
      it("Should be able to react to a change of source"){
        stdNet.chgSensorValue("source", Set(0), true)
        exec(new TestProgram {
          override def main(): Double = ultGradient(sense("source"))
        }, ntimes = fewRounds)(stdNet)

        stdNet.chgSensorValue("source", Set(0), false)
        stdNet.chgSensorValue("source", Set(4), true)
        //Note: requires more iterations than the the others
        exec(new TestProgram {
          override def main(): Double = ultGradient(sense("source"))
        }, ntimes = manyRounds)(stdNet)

        assertNetworkValues((0 to 8).zip(List(
          1.41, 1.0, 1.41,
          1.0, 0.0, 1.0,
          1.41, 1.0, infinity
        )).toMap, Some((d1:Double, d2:Double) => d1===d2 +- 0.01))(stdNet)
      }
    }
  }

  /*
  describe("test refactor"){
    it("bis same as classic") {
      //sameAsClassic(bisGradient(sense("source")))
    }
    it
  }
  */

}
