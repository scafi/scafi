package it.unibo.scafi.test.functional.stdlib

import it.unibo.scafi.test.FunctionalTestIncarnation._
import it.unibo.scafi.test.functional.{ScafiAssertions, ScafiTestUtils}
import org.scalatest._
import org.scalatest.funspec.AnyFunSpec

class TestGradient extends AnyFunSpec with BeforeAndAfterEach {
  import ScafiAssertions._
  import ScafiTestUtils._

  private[this] class SimulationContextFixture(seeds: Seeds) {
    implicit val net: Network with SimulatorOps = manhattanNet(detachedNodesCoords = Set((2,2)), seeds = seeds)
    net.addSensor(name = "source", value = false)
  }

  val infinity: Double = Double.PositiveInfinity

  for(s <- seeds) {
    val seeds = Seeds(s, s, s)
    describe(s"Gradient for $seeds") {
      it should behave like behaviours(seeds)
    }
  }

  private[this] trait TestProgram extends AggregateProgram with StandardSensors with BlockG with Gradients

  def behaviours(seeds: Seeds): Unit = {
    describe("Classic Gradient") {
      def executeOnNet(net: Network with SimulatorOps): Network = exec(new TestProgram {
        override def main(): Double = classicGradient(sense("source"))
      }, ntimes = manyRounds)(net)._1

      describe("On a manhattan network with SW node detached") {
        it("Should be possible to build a gradient of distances from node 0") {
          new SimulationContextFixture(seeds) {
            net.chgSensorValue("source", Set(0), true)
            executeOnNet(net)

            assertNetworkValues((0 to 8).zip(List(
              0.0, 1.0, 2.0,
              1.0, 1.41, 2.41,
              2.0, 2.42, infinity
            )).toMap, Some((d1: Double, d2: Double) => d1 === d2 +- 0.01))(net)
          }
        }
        it("Should be possible to build a gradient of distances from node 4") {
          new SimulationContextFixture(seeds) {
            net.chgSensorValue("source", Set(4), true)
            executeOnNet(net)

            assertNetworkValues((0 to 8).zip(List(
              1.41, 1.0, 1.41,
              1.0, 0.0, 1.0,
              1.41, 1.0, infinity
            )).toMap, Some((d1: Double, d2: Double) => d1 === d2 +- 0.01))(net)
          }
        }
        it("Should return a constant field if no source is selected") {
          new SimulationContextFixture(seeds) {
            executeOnNet(net)

            assertNetworkValues((0 to 8).zip(List(
              infinity, infinity, infinity,
              infinity, infinity, infinity,
              infinity, infinity, infinity
            )).toMap, Some((d1: Double, d2: Double) => d1 === d2 +- 0.01))(net)
          }
        }
        it("Should build a gradient for each source, node 0 and 4"){
          new SimulationContextFixture(seeds) {
            net.chgSensorValue("source", Set(0, 4), true)
            executeOnNet(net)

            assertNetworkValues((0 to 8).zip(List(
              0.0, 1.0, 1.41,
              1.0, 0.0, 1.0,
              1.41, 1.0, infinity
            )).toMap, Some((d1: Double, d2: Double) => d1 === d2 +- 0.01))(net)
          }
        }
        it("Should be able to react to a change of source") {
          new SimulationContextFixture(seeds) {
            net.chgSensorValue("source", Set(0), true)
            executeOnNet(net)

            net.chgSensorValue("source", Set(0), false)
            net.chgSensorValue("source", Set(4), true)
            executeOnNet(net)

            assertNetworkValues((0 to 8).zip(List(
              1.41, 1.0, 1.41,
              1.0, 0.0, 1.0,
              1.41, 1.0, infinity
            )).toMap, Some((d1: Double, d2: Double) => d1 === d2 +- 0.01))(net)
          }
        }
      }
    }
    describe("Hop Gradient"){
      def executeOnNet(net: Network with SimulatorOps): Network = exec(new TestProgram {
        override def main(): Double = hopGradient(sense[Boolean]("source"))
      }, ntimes = manyRounds)(net)._1

      describe("On Standard Network"){
        it("Should be possible to build a gradient of distances from 0") {
          new SimulationContextFixture(seeds) {
            net.chgSensorValue("source", Set(0), true)
            executeOnNet(net)

            assertNetworkValues((0 to 8).zip(List(
              0.0, 1.0, 2.0,
              1.0, 1.0, 2.0,
              2.0, 2.0, infinity
            )).toMap)(net)
          }
        }
        it("Should be possible to build a gradient of distances from node 4") {
          new SimulationContextFixture(seeds) {
            net.chgSensorValue("source", Set(4), true)
            executeOnNet(net)

            assertNetworkValues((0 to 8).zip(List(
              1.0, 1.0, 1.0,
              1.0, 0.0, 1.0,
              1.0, 1.0, infinity
            )).toMap)(net)
          }
        }
        it("Should return a constant field if no source is selected") {
          new SimulationContextFixture(seeds) {
            executeOnNet(net)

            assertNetworkValues((0 to 8).zip(List(
              infinity, infinity, infinity,
              infinity, infinity, infinity,
              infinity, infinity, infinity
            )).toMap)(net)
          }
        }
        it("Should build a gradient for each source, node 0 and 4"){
          new SimulationContextFixture(seeds) {
            net.chgSensorValue("source", Set(0, 4), true)
            executeOnNet(net)

            assertNetworkValues((0 to 8).zip(List(
              0.0, 1.0, 1.0,
              1.0, 0.0, 1.0,
              1.0, 1.0, infinity
            )).toMap)(net)
          }
        }
        it("Should be able to react to a change of source"){
          new SimulationContextFixture(seeds) {
            net.chgSensorValue("source", Set(0), true)
            executeOnNet(net)

            net.chgSensorValue("source", Set(0), false)
            net.chgSensorValue("source", Set(4), true)
            executeOnNet(net)

            assertNetworkValues((0 to 8).zip(List(
              1.0, 1.0, 1.0,
              1.0, 0.0, 1.0,
              1.0, 1.0, infinity
            )).toMap, Some((d1: Double, d2: Double) => d1 === d2 +- 0.01))(net)
          }
        }
      }
    }

    describe("BIS Gradient - refactor") {
      describe("On a manhattan network with SW node detached") {
        testBasicBehaviour(new TestProgram {
          override def main(): (Double, Double) =
            (BisGradient().from(sense("source")).run(), ClassicGradient.from(sense[Boolean]("source")).run())
        })
      }
    }

    describe("CRF Gradient - refactor") {
      describe("On a manhattan network with SW node detached") {
        testBasicBehaviour(new TestProgram {
          //notice the use of an high raisingSpeed
          override def main(): (Double, Double) =
            (CrfGradient(raisingSpeed = 500).from(sense("source")).run(), ClassicGradient.from(sense[Boolean]("source")).run())
        })
      }
    }

    describe("Flex Gradient - refactor") {
      describe("On a manhattan network with SW node detached") {
        testBasicBehaviour(new TestProgram {
          override def main(): (Double, Double) =
            (FlexGradient(epsilon = 0.05).from(sense[Boolean]("source")).run(), ClassicGradient.from(sense[Boolean]("source")).run())
        })
      }
    }

    describe("SVD Gradient - refactor") {
      describe("On a manhattan network with SW node detached") {
        testBasicBehaviour(new TestProgram {
          override def main(): (Double, Double) =
            (SvdGradient().from(sense[Boolean]("source")).run(), ClassicGradient.from(sense[Boolean]("source")).run())
        })
      }
    }
    describe("ULT Gradient - refactor") {
      describe("On a manhattan network with SW node detached") {
        testBasicBehaviour(new TestProgram {
          override def main(): (Double, Double) =
            (UltGradient().from(sense[Boolean]("source")).run(), ClassicGradient.from(sense[Boolean]("source")).run())
        })
      }
    }


    def testBasicBehaviour(v: TestProgram, ntimes: Int = manyManyRounds, tolerance: Double = 0.1): Unit  = {
      it("Should be possible to build a gradient of distances from node 0") {
        new SimulationContextFixture(seeds) {
          net.chgSensorValue("source", Set(0), true)
          exec(v, ntimes = ntimes)(net)
          assert(comparePairResult(net.valueMap[(Double, Double)](), tolerance))
        }
      }
      it("Should be possible to build a gradient of distances from node 4") {
        new SimulationContextFixture(seeds) {
          net.chgSensorValue("source", Set(4), true)
          exec(v, ntimes = ntimes)(net)
          assert(comparePairResult(net.valueMap[(Double, Double)](), tolerance))
        }
      }
      it("Should return a constant field if no source is selected") {
        new SimulationContextFixture(seeds) {
          exec(v, ntimes = ntimes)(net)
          assert(comparePairResult(net.valueMap[(Double, Double)](), tolerance))
        }
      }
      it("Should build a gradient for each source, node 0 and 4"){
        new SimulationContextFixture(seeds) {
          net.chgSensorValue("source", Set(0, 4), true)
          exec(v, ntimes = ntimes)(net)
          assert(comparePairResult(net.valueMap[(Double, Double)](), tolerance))
        }
      }
      it("Should be able to react to a change of source"){
        new SimulationContextFixture(seeds) {
          net.chgSensorValue("source", Set(0), true)
          exec(v, ntimes = ntimes)(net)
          assert(comparePairResult(net.valueMap[(Double, Double)](), tolerance))
          net.chgSensorValue("source", Set(0), false)
          net.chgSensorValue("source", Set(4), true)
          exec(v, ntimes = ntimes)(net)
          assert(comparePairResult(net.valueMap[(Double, Double)](), tolerance))
        }
      }
    }
  }

  def comparePairResult(valueMap:Map[ID, (Double, Double)], tolerance: Double): Boolean = {
    valueMap.forall(e => {
      if (e._2._1 === e._2._2 +- tolerance) {
        true
      } else {
        info(valueMap.toString())
        fail(s"Error on ${e._1}: expected ${e._2._2} but got ${e._2._1}" )
      }
    })
  }
}
