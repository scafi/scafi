/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.test.functional

import it.unibo.scafi.config.GridSettings
import it.unibo.scafi.test.FunctionalTestIncarnation._
import org.scalatest._

import scala.collection.{Map => M}

class TestEdgeFields extends FlatSpec with Matchers {
  import ScafiAssertions._
  import ScafiTestUtils._

  val EdgeFields = new ItWord
  val stepx: Double = 1.0
  val stepy: Double = 1.5
  val SRC = "source"
  val FLAG = "flag"
  val (fewRounds, someRounds, manyRounds, manyManyRounds) = (100, 500, 1000, 2000)

  private[this] trait SimulationContextFixture {
    val net: Network with SimulatorOps =
      SetupNetwork(simulatorFactory.gridLike(GridSettings(3, 3, stepx, stepy), rng = 1.6))
  }

  private[this] trait TestLib { self: AggregateProgram with EdgeFields with StandardSensors =>
    import scala.math.Numeric._

    type Field[T] = EdgeField[T]

    def gradient(source: Field[Boolean]): Field[Double] =
      rep(Double.PositiveInfinity){ // automatic local-to-field conversion
        d => mux(source) { 0.0 } {
          (fnbr(d) + fsns(nbrRange, Double.PositiveInfinity)).minHoodPlus
        }
      }
  }

  private[this] trait TestProgram extends AggregateProgram with EdgeFields with StandardSensors with TestLib

  def SetupNetwork(n: Network with SimulatorOps) = {
    n.addSensor(SRC, false)
    n.chgSensorValue(SRC, ids = Set(8), value = true)
    n.addSensor(FLAG, false)
    n.chgSensorValue(FLAG, ids = Set(0,1,2,3,4), value = true)
    n
  }

  EdgeFields should "enable keeping track of unidirectional connection times" in new SimulationContextFixture {
    val p = new TestProgram {
      override def main(): Int = exchangeFull(0)(p => p.old + defSubs(1,0))
    }

    runProgramInOrder(Seq(0,1,2), p)(net)

    assertNetworkValues((0 to 8).zip(List(
      6,    5,    4,
      4.5,  3.5,  2.5,
      3,    2,    1
    )).toMap)(net)
  }

  EdgeFields should "enable keeping track of bidirectional connection times" in new SimulationContextFixture {
    val p = new TestProgram {
      override def main(): Int = exchange(0)(n => n + defSubs(1,0))
    }

    runProgramInOrder(Seq(0,1,2), p)(net)

    assertNetworkValues((0 to 8).zip(List(
      6,    5,    4,
      4.5,  3.5,  2.5,
      3,    2,    1
    )).toMap)(net)
  }

  // TODO
  EdgeFields should "subsume nbr" in new SimulationContextFixture {
    exec(new TestProgram {
      override def main(): Double = ???
    }, ntimes = someRounds)(net)

    assertNetworkValues((0 to 8).zip(List(
      6,    5,    4,
      4.5,  3.5,  2.5,
      3,    2,    1
    )).toMap)(net)
  }

  // TODO
  EdgeFields should "subsume rep" in new SimulationContextFixture {
    val p = new TestProgram {
      override def main(): Int = repByExchange(0)(_+1)
    }

    val s = schedulingSequence(net.ids, 1000)

    runProgramInOrder(s, p)(net)

    assertForAllNodes((id,v: Int) => s.count(_==id)==v)(net)
  }

  // TODO
  EdgeFields should "subsume share" in new SimulationContextFixture {
    val p = new TestProgram {
      override def main(): Int = shareByExchange(0)(_+1)
    }

    val s = schedulingSequence(net.ids, 1000)

    runProgramInOrder(s, p)(net)

    assertForAllNodes((id,v: Int) => s.count(_==id)==v)(net)
  }

  EdgeFields should "support construction of gradients" in new SimulationContextFixture {
    // ACT
    exec(new TestProgram {
      override def main(): Double = gradient(sense[Boolean](SRC)) + 1
    }, ntimes = someRounds)(net)

    // ASSERT
    assertNetworkValues((0 to 8).zip(List(
      6,    5,    4,
      4.5,  3.5,  2.5,
      3,    2,    1
    )).toMap)(net)
  }

  EdgeFields should "allow going from smaller to larger domains" in new SimulationContextFixture {
    // ACT
    exec(new TestProgram {
      override def main() = branch(sense[Boolean](FLAG)){
        fnbr(1.0).withoutSelf
      }{
        fnbr(-1.0)
      }.fold(0.0)(_ + _)
    }, ntimes = someRounds)(net)

    // ASSERT
    assertNetworkValues((0 to 8).zip(List(
      2,    3,    1,
      2,    2,   -2,
      -2,   -3,   -3
    )).toMap)(net)
  }

  EdgeFields should "deal with domain mismatches" in new SimulationContextFixture {
    an [Exception] should be thrownBy exec(new TestProgram {
      override def main(): Double = {
        val f1 = branch(sense[Boolean](FLAG)){ fnbr(1.0).withoutSelf }{ fnbr(-1.0) }
        val f2 = branch(sense[Boolean](SRC)){ fnbr(10.0) }{ fnbr(0.0) }
        (f1.map2(f2)(_ + _)).fold(0.0)(_ + _)
      }
    }, ntimes = someRounds)(net)
  }

  EdgeFields should "support defaults to deal with domain mismatches" in new SimulationContextFixture {
    // ACT
    exec(new TestProgram {
      override def main(): Map[ID,String] = {
        val f1: Field[String] = branch(sense[Boolean](FLAG)){ fnbr("a").withoutSelf }{ fnbr("b") }
        val f2: Field[String] = branch(!sense[Boolean](SRC)){ fnbr("c") }{ fnbr("d") }
        (f1.map2d(f2)("x")(_ + _)).toMap
      }
    }, ntimes = someRounds)(net)

    // ASSERT
    assertNetworkValues((0 to 8).zip(List(
      M(1->"ac", 3->"ac"), M(0->"ac", 2->"ac", 4->"ac"), M(1->"ac"),
      M(4->"ac", 0->"ac"), M(1->"ac", 3->"ac"),          M(5->"bc", 8->"bx"),
      M(6->"bc", 7->"bc"), M(6->"bc", 7->"bc", 8->"bx"), M(5->"bx", 7->"bx", 8->"bd")
    )).toMap)(net)
  }

  EdgeFields should "support defaults on both sides to deal with domain mismatches" in new SimulationContextFixture {
    // ACT
    exec(new TestProgram {
      override def main(): Map[ID,String] = {
        val f1: Field[String] = branch(sense[Boolean](FLAG)){ fnbr("a").withoutSelf }{ fnbr("b") }
        val f2: Field[String] = branch(!sense[Boolean](SRC)){ fnbr("c") }{ fnbr("d") }
        (f1.map2u(f2)("x","w")(_ + _)).toMap
      }
    }, ntimes = someRounds)(net)

    // ASSERT
    assertNetworkValues((0 to 8).zip(List(
      M(0->"xc",1->"ac",3->"ac"),         M(0->"ac",1->"xc",2->"ac",4->"ac"),         M(1->"ac",2->"xc",5->"xc"),
      M(0->"ac",3->"xc",4->"ac",6->"xc"), M(1->"ac",3->"ac",4->"xc",5->"xc",7->"xc"), M(2->"xc",4->"xc",5->"bc",8->"bw"),
      M(3->"xc",6->"bc",7->"bc"),         M(4->"xc",6->"bc",7->"bc",8->"bw"),         M(5->"bw",7->"bw",8->"bd")
    )).toMap)(net)
  }

  EdgeFields should "support restriction by going from larger to smaller domains" in new SimulationContextFixture {
    // ACT
    exec(new TestProgram {
      override def main(): Any = {
        val phi: Field[String] = fnbr(if(sense[Boolean](FLAG)) "a" else "b")
        val f1 = (x: Field[String]) => aggregate{ x.fold("")(_+_).sorted }
        val f2 = (x: Field[String]) => aggregate{ x.fold("")(_+_).sorted.toUpperCase }

        val numphi: Field[Int] = fnbr(if(sense[Boolean](FLAG)) 1 else 2)
        val phi2: Field[String] = branch(mid<=3){ numphi+0 }{ numphi+5 }.map(_.toString)

        // 1+0  |  1+0    1+0
        //      |------------
        // 1+0  |  1+5    2+5
        // -----|
        // 2+5  |  2+5    2+5


        ((mux(mid%3==0){ f1 }{ f2 })(phi),
          (mux(mid%3==0){ f1 }{ f2 })(phi2))
      }
    }, ntimes = someRounds)(net)

    // ASSERT
    assertNetworkValues((0 to 8).zip(List(
      ("aa", "11"), ("AAA",   "11"),  ("AAB",   "11"),
      ("aab","11"), ("AABB", "677") , ("AABB", "677"),
      ("ab",  "7"), ("ABB",  "677"),  ("BBB",  "777")
    )).toMap)(net)
  }
}
