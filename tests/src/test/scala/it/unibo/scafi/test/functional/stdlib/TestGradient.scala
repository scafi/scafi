package it.unibo.scafi.test.functional.stdlib

import it.unibo.scafi.test.FunctionalTestIncarnation._
import it.unibo.scafi.test.functional.{ScafiAssertions, ScafiTestUtils}
import org.scalatest._

class TestGradient extends FunSpec with BeforeAndAfterEach {
  import ScafiAssertions._
  import ScafiTestUtils._

  var stdNet: Network with SimulatorOps = manhattanNet(detachedNodesCords = Set((2,2)))
  val infinity: Double = Double.PositiveInfinity

  def restartNetwork(): Unit = {
    stdNet = manhattanNet(detachedNodesCords = Set((2,2)))
    stdNet.addSensor(name = "source", value = false)
  }
  override protected def beforeEach(): Unit = restartNetwork()

  private[this] trait TestProgram extends AggregateProgram with StandardSensors with BlockG with Gradients

  describe("Classic Gradient") {
    describe("On a manhattan network with SW node detached") {
      it("Should be possible to build a gradient of distances from node 0") {
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
      it("Should be possible to build a gradient of distances from node 4") {
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
      it("Should be possible to build a gradient of distances from 0") {
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
      it("Should be possible to build a gradient of distances from node 4") {
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

  describe("BIS Gradient - refactor") {
    describe("On a manhattan network with SW node detached") {
      testBasicBehaviour(new TestProgram {
        override def main(): (Double, Double) = (BisGradient().from(sense("source")).run(), ClassicGradient.from(sense[Boolean]("source")).run())
      })
    }
  }

  describe("CRF Gradient - refactor") {
    describe("On a manhattan network with SW node detached") {
      testBasicBehaviour(new TestProgram {
        //notice the use of an high raisingSpeed
        override def main(): (Double, Double) = (CrfGradient(raisingSpeed = 500).from(sense("source")).run(), ClassicGradient.from(sense[Boolean]("source")).run())
      })
    }
  }

  describe("Flex Gradient - refactor") {
    describe("On a manhattan network with SW node detached") {
      testBasicBehaviour(new TestProgram {
        override def main(): (Double, Double) = (FlexGradient(epsilon = 0.05).from(sense[Boolean]("source")).run(), ClassicGradient.from(sense[Boolean]("source")).run())
      })
    }
  }

  describe("SVD Gradient - refactor") {
    describe("On a manhattan network with SW node detached") {
      testBasicBehaviour(new TestProgram {
        override def main(): (Double, Double) = (SvdGradient().from(sense[Boolean]("source")).run(), ClassicGradient.from(sense[Boolean]("source")).run())
      })
    }
  }
  describe("ULT Gradient - refactor") {
    describe("On a manhattan network with SW node detached") {
      testBasicBehaviour(new TestProgram {
        override def main(): (Double, Double) = (UltGradient().from(sense[Boolean]("source")).run(), ClassicGradient.from(sense[Boolean]("source")).run())
      })
    }
  }

  /*TODO
  def testBasicBehaviour(gradient: Gradient, ntimes: Int = manyRounds, tolerance: Double = 0.1): Unit  = {
    val v: TestProgram = new TestProgram {
      override def main(): (Double, Double) = (gradient.from(sense("source")).run(), classicGradient(sense("source")))
    }*/
  def testBasicBehaviour(v: TestProgram, ntimes: Int = manyManyRounds, tolerance: Double = 0.1): Unit  = {
    it("Should be possible to build a gradient of distances on node 0") {
      stdNet.chgSensorValue("source", Set(0), true)
      exec(v, ntimes = ntimes)(stdNet)
      assert(comparePairResult(stdNet.valueMap[(Double, Double)](), tolerance))
    }
    it("Should be possible to build a gradient of distances on node 4") {
      stdNet.chgSensorValue("source", Set(4), true)
      exec(v, ntimes = ntimes)(stdNet)
      assert(comparePairResult(stdNet.valueMap[(Double, Double)](), tolerance))
    }
    it("Should return a constant field if no source is selected") {
      exec(v, ntimes = ntimes)(stdNet)
      assert(comparePairResult(stdNet.valueMap[(Double, Double)](), tolerance))
    }
    it("Should build a gradient for each source, node 0 and 4"){
      stdNet.chgSensorValue("source", Set(0, 4), true)
      exec(v, ntimes = ntimes)(stdNet)
      assert(comparePairResult(stdNet.valueMap[(Double, Double)](), tolerance))
    }
    it("Should be able to react to a change of source"){
      stdNet.chgSensorValue("source", Set(0), true)
      exec(v, ntimes = ntimes)(stdNet)
      assert(comparePairResult(stdNet.valueMap[(Double, Double)](), tolerance))
      stdNet.chgSensorValue("source", Set(0), false)
      stdNet.chgSensorValue("source", Set(4), true)
      exec(v, ntimes = ntimes)(stdNet)
      assert(comparePairResult(stdNet.valueMap[(Double, Double)](), tolerance))
    }

  }

  def comparePairResult(valueMap:Map[ID, (Double, Double)], tolerance: Double): Boolean = {
    valueMap.forall(e => {
      if (e._2._1 === e._2._2 +- tolerance) {
        true
      } else {
        println(valueMap)
        fail(s"Error on ${e._1}: expected ${e._2._2} but got ${e._2._1}" )
      }
    })
  }
}
