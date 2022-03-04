/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.test.functional

import it.unibo.scafi.config.GridSettings
import it.unibo.scafi.test.FunctionalTestIncarnation._
import org.scalatest._
import scala.collection.{Map=>M}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class TestExplicitFields extends AnyFlatSpec with Matchers {
  import ScafiAssertions._
  import ScafiTestUtils._

  val ExplicitFields = new ItWord
  val stepx: Double = 1.0
  val stepy: Double = 1.5
  val SRC = "source"
  val FLAG = "flag"
  val (fewRounds, someRounds, manyRounds, manyManyRounds) = (100, 500, 1000, 2000)

  private[this] class SimulationContextFixture(seeds: Seeds) {
    val net: Network with SimulatorOps =
      setupNetwork(simulatorFactory.gridLike(GridSettings(3, 3, stepx, stepy), rng = 1.6, seeds = seeds))
  }

  private[this] trait TestLib { self: AggregateProgram with ExplicitFields with StandardSensors =>
    import scala.math.Numeric._

    def gradient(source: Field[Boolean]): Field[Double] =
      rep(Double.MaxValue){ // automatic local-to-field conversion
        d => mux(source) { 0.0 } {
          (fnbr(d) + fsns(nbrRange)).minHoodPlus
        }
      }
  }

  private[this] trait TestProgram extends AggregateProgram with ExplicitFields with StandardSensors with TestLib

  def setupNetwork(n: Network with SimulatorOps) = {
    n.addSensor(SRC, false)
    n.chgSensorValue(SRC, ids = Set(8), value = true)
    n.addSensor(FLAG, false)
    n.chgSensorValue(FLAG, ids = Set(0,1,2,3,4), value = true)
    n
  }

  for(s <- seeds) {
    val seeds = Seeds(s, s, s)
    behavior of s"Explicit fields for $seeds"
    it should behave like behaviours(seeds)
  }

  def behaviours(seeds: Seeds): Unit = {
    ExplicitFields should "support construction of gradients" in new SimulationContextFixture(seeds) {
      exec(new TestProgram {
        override def main(): Double = gradient(sense[Boolean](SRC)) + 1
      }, ntimes = someRounds)(net)

      assertNetworkValues((0 to 8).zip(List(
        6,    5,    4,
        4.5,  3.5,  2.5,
        3,    2,    1
      )).toMap)(net)
    }

    ExplicitFields should "allow going from smaller to larger domains" in new SimulationContextFixture(seeds) {
      exec(new TestProgram {
        override def main() = branch(sense[Boolean](FLAG)){
          fnbr(1.0).withoutSelf
        }{
          fnbr(-1.0)
        }.fold(0.0)(_ + _)
      }, ntimes = someRounds)(net)

      assertNetworkValues((0 to 8).zip(List(
        2,    3,    1,
        2,    2,   -2,
        -2,   -3,   -3
      )).toMap)(net)
    }

    ExplicitFields should "deal with domain mismatches" in new SimulationContextFixture(seeds) {
      an[Exception] should be thrownBy exec(new TestProgram {
        override def main(): Double = {
          val f1 = branch(sense[Boolean](FLAG)){ fnbr(1.0).withoutSelf }{ fnbr(-1.0) }
          val f2 = branch(sense[Boolean](SRC)){ fnbr(10.0) }{ fnbr(0.0) }
          (f1.map2(f2)(_ + _)).fold(0.0)(_ + _)
        }
      }, ntimes = someRounds)(net)
    }

    ExplicitFields should "support defaults to deal with domain mismatches" in new SimulationContextFixture(seeds) {
      exec(new TestProgram {
        override def main(): Map[ID,String] = {
          val f1: Field[String] = branch(sense[Boolean](FLAG)){ fnbr("a").withoutSelf }{ fnbr("b") }
          val f2: Field[String] = branch(!sense[Boolean](SRC)){ fnbr("c") }{ fnbr("d") }
          (f1.map2d(f2)("x")(_ + _)).toMap
        }
      }, ntimes = someRounds)(net)

      assertNetworkValues((0 to 8).zip(List(
        M(1->"ac", 3->"ac"), M(0->"ac", 2->"ac", 4->"ac"), M(1->"ac"),
        M(4->"ac", 0->"ac"), M(1->"ac", 3->"ac"),          M(5->"bc", 8->"bx"),
        M(6->"bc", 7->"bc"), M(6->"bc", 7->"bc", 8->"bx"), M(5->"bx", 7->"bx", 8->"bd")
      )).toMap)(net)
    }

    ExplicitFields should "support defaults on both sides to deal with domain mismatches" in new SimulationContextFixture(seeds) {
      exec(new TestProgram {
        override def main(): Map[ID,String] = {
          val f1: Field[String] = branch(sense[Boolean](FLAG)){ fnbr("a").withoutSelf }{ fnbr("b") }
          val f2: Field[String] = branch(!sense[Boolean](SRC)){ fnbr("c") }{ fnbr("d") }
          (f1.map2u(f2)("x","w")(_ + _)).toMap
        }
      }, ntimes = someRounds)(net)

      assertNetworkValues((0 to 8).zip(List(
        M(0->"xc",1->"ac",3->"ac"),         M(0->"ac",1->"xc",2->"ac",4->"ac"),         M(1->"ac",2->"xc",5->"xc"),
        M(0->"ac",3->"xc",4->"ac",6->"xc"), M(1->"ac",3->"ac",4->"xc",5->"xc",7->"xc"), M(2->"xc",4->"xc",5->"bc",8->"bw"),
        M(3->"xc",6->"bc",7->"bc"),         M(4->"xc",6->"bc",7->"bc",8->"bw"),         M(5->"bw",7->"bw",8->"bd")
      )).toMap)(net)
    }

    ExplicitFields should "support restriction by going from larger to smaller domains" in new SimulationContextFixture(seeds) {
      exec(new TestProgram {
        override def main(): Any = {
          val phi: Field[String] = fnbr(if(sense[Boolean](FLAG)) "a" else "b")
          val f1 = (x: Field[String]) => aggregate{ x.fold("")(_ + _).sorted }
          val f2 = (x: Field[String]) => aggregate{ x.fold("")(_ + _).sorted.toUpperCase }

          val numphi: Field[Int] = fnbr(if(sense[Boolean](FLAG)) 1 else 2)
          val phi2: Field[String] = branch(mid<=3){ numphi + 0 }{ numphi + 5 }.map(_.toString)

          // 1+0  |  1+0    1+0
          //      |------------
          // 1+0  |  1+5    2+5
          // -----|
          // 2+5  |  2+5    2+5

          ((mux(mid%3==0){ f1 }{ f2 })(phi),
            (mux(mid%3==0){ f1 }{ f2 })(phi2))
        }
      }, ntimes = someRounds)(net)

      assertNetworkValues((0 to 8).zip(List(
        ("aa", "11"), ("AAA",   "11"),  ("AAB",   "11"),
        ("aab","11"), ("AABB", "677") , ("AABB", "677"),
        ("ab",  "7"), ("ABB",  "677"),  ("BBB",  "777")
      )).toMap)(net)
    }
  }
}
