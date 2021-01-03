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

  private[this] trait TestProgram extends AggregateProgram with EdgeFields with StandardSensors

  def SetupNetwork(n: Network with SimulatorOps) = {
    n.addSensor(SRC, false)
    n.chgSensorValue(SRC, ids = Set(8), value = true)
    n.addSensor(FLAG, false)
    n.chgSensorValue(FLAG, ids = Set(0,1,2,3,4), value = true)
    n
  }

  /** NB:
    * Edge labels are assigned from destination nodes and differ from those that are actually sent.
    * Unidirectional connection time for a single node:
    *     (0) -[1]-> (0) -[2]-> (0) -[3]-> ...     (so it's the num of events)
    *
    * Unidirection connection time between two nodes
    *     (0)
    *        \ [0] by the perspective of (0), but [1] by the perspective of (1), thanks to defSubs
    *         (1)
    *            \ [0] by the perspective of (1), but [1] by the perspective of (0), thanks to defSubs
    *             (0)
    *               \ [1] by the perspective of (0), but [2] by the perspective of (1), thanks to defSubs
    *                (1)
    *                  \ [3]
    *
    * NB: In the tests that follow we check the `EdgeField` that is being sent by any given device
    * (so, it differs from event structure representations where edge labels are on incoming edges wrt target events)
    *
    * There are 4 cases of sequences (consider two connected devices 0 and 1, and consider 0 as target device),
    *  depending on what is the starting and ending device ID in the sequence:
    *
    * 1) 1-0-0-1 (nbr starts and ends the seq)                ==> [1,0,0] (correct answer: 2, i.e. the num of zeros)
    * 2) 1-1-0-0 (nbr starts the seq)                         ==> [1,0,0] (same)
    * 3) 0-0-1-1-0-1 (nbr does not start but ends the seq)    ==> [1,0] (correct answer: 1, i.e., the num of zeros)
    * 4) 0-0-1-1-0 (nbr does not start not ends the seq)      ==> [1,0] (same)
    *
    * The useful part of the sequences are shown to the right. This is kept by keeping the seq after the first occurrance
    *  of the neighbour, and then counting the num of times that `self` occurs.
    * For self wrt self itself, just count the num of self events.
    */
  EdgeFields should "enable keeping track of unidirectional connection times" in new SimulationContextFixture {
    val p = new TestProgram {
      /**
        * @return the count of rounds a device is receiving messages from each of its neighbours
        */
      override def main(): Map[ID,Int] = exchangeFull(0)(p => p.old + defSubs(1,0)).toMap
    }

    val s = //List(1,0,0,1)
      schedulingSequence(net.ids, 100).toList

    runProgramInOrder(s, p)(net)

    assertForAllNodes((id,v: Map[ID,Int]) =>
      try {
        v == (net.inputNeighbours(id)).map(nbrId => nbrId -> {
          val filteredSeq = s.filter(Set(id, nbrId).contains(_))
          if(id == nbrId) {
            filteredSeq.count(_ == id)
          } else {
            val relevantSeq = filteredSeq.drop(filteredSeq.indexOf(nbrId)).filter(_ == id)
            relevantSeq.length
          }
        }).toMap
      } catch { case e => e.printStackTrace(); true},
      okWhenNotComputed = true,
      msg = s"Run sequence: ${s}")(net)
  }

  EdgeFields should "enable keeping track of bidirectional connection times" in new SimulationContextFixture {
    val p = new TestProgram {
      /**
        * @return number of times messages bounces between any pair of neighbour
        */
      override def main(): Map[ID,Int] = exchange(0)(n => n + defSubs(1,0)).toMap
    }

    val s = List(0,1,0,1) // Seq(1,0,1,2,1,2,0,1,2,1,2,0,1)
      // schedulingSequence(net.ids, 10)

    runProgramInOrder(s, p)(net)

    assertForAllNodes((id,v: Map[ID,Int]) =>
      v == (net.neighbourhood(id)).map(nbrId => nbrId -> {
        var baseSeq = s.filter(Set(id, nbrId).contains(_))
        if (id != nbrId) { baseSeq = dropConsecutiveEquals(baseSeq) }

        val m = if(id == nbrId) baseSeq.size
        else if(id != nbrId && baseSeq.size >= 2) baseSeq.sliding(2).count(s => s(0)!=s(1))
        else 0
        // println(s"$id :: $nbrId -> $m ($baseSeq)")
        m
      }).filter(_._2 > 0).toMap,
      okWhenNotComputed = true,
      msg = s"Run sequence: ${s}")(net)
  }

  EdgeFields should "subsume nbr (lifted)" in new SimulationContextFixture {
    val p = new TestProgram {
      override def main() = nbrByExchange(mid).toMap
    }

    val s = List.fill(2)(net.ids).flatten

    runProgramInOrder(s, p)(net)

    assertNetworkValues((0 to 8).zip(List(
      Map(0->0, 1->1, 3->3),       Map(0->0, 1->1, 2->2, 4->4),       Map(1->1, 2->2, 5->5),
      Map(0->0, 3->3, 4->4, 6->6), Map(1->1, 3->3, 4->4, 5->5, 7->7), Map(2->2, 4->4, 5->5, 8->8),
      Map(3->3, 6->6, 7->7),       Map(4->4, 6->6, 7->7, 8->8),       Map(7->7, 5->5, 8->8)
    )).toMap, msg = s"Run sequence: ${s}")(net)
  }

  EdgeFields should "subsume nbr (original)" in new SimulationContextFixture {
    val p = new TestProgram {
      override def main() = nbrLocalByExchange(mid).toMap
    }

    val s = List.fill(2)(net.ids).flatten

    runProgramInOrder(s, p)(net)

    assertNetworkValues((0 to 8).zip(List(
      Map(0->0, 1->1, 3->3),       Map(0->0, 1->1, 2->2, 4->4),       Map(1->1, 2->2, 5->5),
      Map(0->0, 3->3, 4->4, 6->6), Map(1->1, 3->3, 4->4, 5->5, 7->7), Map(2->2, 4->4, 5->5, 8->8),
      Map(3->3, 6->6, 7->7),       Map(4->4, 6->6, 7->7, 8->8),       Map(7->7, 5->5, 8->8)
    )).toMap, msg = s"Run sequence: ${s}")(net)
  }


  EdgeFields should "subsume nbr (lifted + fold)" in new SimulationContextFixture {
    val p = new TestProgram {
      override def main() = nbrByExchange(mid).fold[ID](Int.MinValue)((i1,i2) => Math.max(i1,i2))
    }

    val s = List.fill(2)(net.ids).flatten

    runProgramInOrder(s, p)(net)

    assertNetworkValues((0 to 8).zip(List(
      3, 4, 5,
      6, 7, 8,
      7, 8, 8
    )).toMap, msg = s"Run sequence: ${s}")(net)
  }

  EdgeFields should "subsume rep" in new SimulationContextFixture {
    val p = new TestProgram {
      override def main(): Int = repByExchange(0)(_+1)
    }

    val s = schedulingSequence(net.ids, 1000)

    runProgramInOrder(s, p)(net)

    assertForAllNodes((id,v: Int) => s.count(_==id)==v)(net)
  }

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
      override def main(): Int = exchange(Double.PositiveInfinity)(n =>
        mux(sense[Boolean](SRC)){ 0.0 } { n.fold(Double.PositiveInfinity)(Math.min) + 1 }
      ).toInt
    }, ntimes = someRounds)(net)

    // ASSERT
    assertNetworkValues((0 to 8).zip(List(
      4,  3,  2,
      3,  2,  1,
      2,  1,  0
    )).toMap)(net)
  }

  private def dropConsecutiveEquals[T](lst: List[T]): List[T] = lst match {
    case a :: b :: tl if a == b => dropConsecutiveEquals(b :: tl)
    case a :: b :: tl => a :: dropConsecutiveEquals(b :: tl)
    case l => l
  }
}
