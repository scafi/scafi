/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.test.functional

import it.unibo.scafi.config.GridSettings
import it.unibo.scafi.test.FunctionalTestIncarnation._
import org.scalactic.TolerantNumerics
import org.scalactic.TypeCheckedTripleEquals.convertToCheckingEqualizer
import org.scalatest._

import scala.collection.{Map => M}

class TestEdgeFields extends FlatSpec with Matchers {
  import ScafiAssertions._
  import ScafiTestUtils._

  val EdgeFields = new ItWord
  val stepx: Double = 1.0
  val stepy: Double = 1.5
  val SRC = "source"
  val OBSTACLE = "obstacle"
  val FLAG = "flag"
  val (fewRounds, someRounds, manyRounds, manyManyRounds) = (100, 500, 1000, 2000)
  val RANGE_RADIUS = 1.6

  private[this] trait SimulationContextFixture {
    val net: Network with SimulatorOps =
      SetupNetwork(simulatorFactory.gridLike(GridSettings(3, 3, stepx, stepy), rng = RANGE_RADIUS))
  }

  private[this] trait TestProgram extends AggregateProgram with EdgeFields with StandardSensors with TestLib

  trait TestLib {
    self: AggregateProgram with EdgeFields with StandardSensors =>

    /**
      * - Every node receives by a single parent
      * - Every node transmits to all those neighbours that chose it as a parent
      * - The parent is chosen as the node with minimum distance
      */
    def optimisedBroadcast[T](distance: Double, value: T, Null: T): T = {
      // (default is the self-key)
      val nbrKey: EdgeField[(Double,ID)] = nbrLocalByExchange((distance, mid))
      // `parent` is a Boolean field that holds true for the device that chose the current device as a parent
      //   (default is the true if the self-key is equal to the min-key---this is true only for the source)
      val parent = nbrKey.map(_ == nbrKey.fold[(Double,ID)](nbrKey)((t1,t2) => if(t1._1 < t2._1) t1 else t2))
      exchange(value)(n => {
        val _loc = n.map2(nbrLocalByExchange(distance)){ case (v,d) => (v == Null, d, v) }
          .fold((false, distance, value))((t1,t2) => if(!t1._1 && t2._1) t1 else if(t1._2 < t2._2) t1 else t2)
        val loc = _loc._3
        val nbrParent = nbrByExchange(parent)
        // (default value of `res` is `Null` for every device except sources)
        val res = selfSubs(nbrParent.map(mux(_) { loc } { Null }), loc) // defSubs(..., Null) ?
        // println(s"${mid} => nbrKey ${nbrKey} parent ${parent} N ${n} _loc ${_loc} nbrParent ${nbrParent} res ${res}")
        res
      })
    }

    def hopGradient(src: Boolean): EdgeField[Int] = exchange(Double.PositiveInfinity)(n =>
      mux(src){ 0.0 } { n.fold(Double.PositiveInfinity)(Math.min) + 1 }
    ).toInt

    def gradient(source: Boolean, metric: EdgeField[Double]): Double = exchange(Double.PositiveInfinity)(n =>
      mux(source){ 0.0 } { (n + metric).fold(Double.PositiveInfinity)(Math.min) }
    )

    def biConnection(): EdgeField[Int] = exchange(0)(n => n + defSubs(1,0))

    def Csubj[P: Builtins.Bounded, V](sink: Boolean, value: V, acc: (V, V) => V, divide: (V,Double) => V): V = {
      val dist: Int = hopGradient(sink)
      // Use exchange to handle communication of distances (dist) and collected values
      exchange[(Int,V)]((dist, value))(n => {
        // The reliability of a downstream neighbor (with lower values of `dist` wrt self) is estimated by the
        //   the corresponding connection time.
        // val conn: EdgeField[Int] = mux(n.map(_._1).fold(Int.MaxValue)(Math.min) < dist){ biConnection() }{ 0 }
        val conn: EdgeField[Int] = pair(n, biConnection()).map{ case (n,biconn) => mux(n._1 < dist){ biconn } { 0 } }
        // Reliability scores are normalised in `send`, obtaining percetanges
        val send: EdgeField[Double] = conn.map(_.toDouble / Math.max(1, conn.fold(0)(_+_)))
        // Let's collect the `send` scores into `recv` scores for receiving neighbours' messages
        val recv: EdgeField[Double] = nbrByExchange(send)
        // Now, values of neighbours (`n.map(_._2)`) are weighted with `recv` scores through given `divide` function
        val weightedValues: EdgeField[V] = pair(n.map(_._2), recv).map(v => divide(v._1, v._2))
        // Finally, use `acc` to aggregate neighbours' contributions
        val collectedValue: V = weightedValues.fold(value)(acc)
        // println(s"${mid} => n = $n dist = ${n._1} conn = $conn send = $send recv = $recv weightedvals = $weightedValues collectedValue = $collectedValue")
        pair(dist : EdgeField[Int], collectedValue)
      })._2
    }

    def nbrRangeEF: EdgeField[Double] = fsns(nbrRange, Double.PositiveInfinity)
  }

  def SetupNetwork(n: Network with SimulatorOps) = {
    n.addSensor(SRC, false)
    n.chgSensorValue(SRC, ids = Set(8), value = true)
    n.addSensor(OBSTACLE, false)
    n.chgSensorValue(OBSTACLE, ids = Set(5), value = true)
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

    val s = schedulingSequence(net.ids, 100).toList

    runProgramInOrder(s, p)(net)

    assertForAllNodes((id,v: Map[ID,Int]) =>
      v == (net.neighbourhood(id)).map(nbrId => nbrId -> {
        var baseSeq = s.filter(Set(id, nbrId).contains(_))
        if(id == nbrId) {
          baseSeq.size
        } else {
          baseSeq = dropConsecutiveEquals(baseSeq)
          baseSeq.size + (if (baseSeq.last == nbrId) -1 else 0) - 1
        }
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

  EdgeFields should "subsume nbr, by ensuring that neighbour-specific messages are shipped" in new SimulationContextFixture {
    val p = new TestProgram {
      override def main(): Map[ID, Boolean] = {
        val edgeValue: EdgeField[Boolean] = if (mid == 0) {
          EdgeField(Map(1 -> true), false)
        } else if (mid == 1) {
          EdgeField(Map(2 -> true), false)
        } else {
          EdgeField(Map(), false)
        }
        nbrByExchange(edgeValue).toMap
      }
    }

    val s = List.fill(3)(Set(0,1,2)).flatten

    runProgramInOrder(s, p)(net)

    assertNetworkValues((0 to 2).zip(List(
      Map(0 -> false, 1 -> false), Map(0 -> true, 1 -> false, 2 -> false), Map(1 -> true, 2 -> false)
    )).toMap, msg = s"Run sequence: ${s}")(net)
  }

  EdgeFields should "subsume nbr (lifted), 2" in new SimulationContextFixture {
    val p = new TestProgram {
      override def main() = {
        val src = sense[Boolean](SRC)
        val distance = gradient(src, nbrRangeEF)
        val nbrKey: EdgeField[(Double,ID)] = nbrLocalByExchange((distance, mid))
        val parent: EdgeField[Boolean] = nbrKey.map(_ == nbrKey.fold[(Double,ID)](nbrKey)((t1,t2) => if(t1._1 < t2._1) t1 else t2))
        // println(s"${mid} distance = ${nbrLocalByExchange(distance)} \n nbrKey = $nbrKey \n parent = $parent \n nbrByExchange ${nbrByExchange(parent).toMap}")
        (parent.toMap, nbrByExchange(parent).toMap)
      }
    }

    val s = schedulingSequence(net.ids, someRounds)

    runProgramInOrder(s, p)(net)

    val TRUE = true
    val F = false
    /* 5.0     4.0     3.0
        v       v       v
       3.5     2.5     1.5
        v       v       v
       2.0 ->  1.0 ->  0.0 */
    assertNetworkValues((0 to 8).zip(List(
      (Map(0->F, 1->F, 3->TRUE),             Map(0->F, 1->F, 3->F)),
      (Map(0->F, 1->F, 2->F, 4->TRUE),       Map(0->F, 1->F, 2->F, 4->F)),
      (Map(1->F, 2->F, 5->TRUE),             Map(1->F, 2->F, 5->F)),
      (Map(0->F, 3->F, 4->F, 6->TRUE),       Map(0->TRUE, 3->F, 4->F, 6->F)),
      (Map(1->F, 3->F, 4->F, 5->F, 7->TRUE), Map(1->TRUE, 3->F, 4->F, 5->F, 7->F)),
      (Map(2->F, 4->F, 5->F, 8->TRUE),       Map(2->TRUE, 4->F, 5->F, 8->F)),
      (Map(3->F, 6->F, 7->TRUE),             Map(3->TRUE, 6->F, 7->F)),
      (Map(4->F, 6->F, 7->F, 8->TRUE),       Map(4->TRUE, 6->TRUE, 7->F, 8->F)),
      (Map(7->F, 5->F, 8->TRUE),             Map(7->TRUE, 5->TRUE, 8->TRUE))
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

  EdgeFields should "support construction of (hop) gradients" in new SimulationContextFixture {
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

  EdgeFields should "support construction of gradients" in new SimulationContextFixture {
    // ACT
    exec(new TestProgram {
      def distanceTo(source: Boolean, metric: EdgeField[Double]): Double = exchange(Double.PositiveInfinity)(n =>
        mux(source){ 0.0 } { (n + metric).fold(Double.PositiveInfinity)(Math.min) }
      )

      override def main(): Double = distanceTo(sense[Boolean](SRC), fsns(nbrRange, Double.PositiveInfinity))
    }, ntimes = someRounds)(net)

    implicit val doubleEquality = TolerantNumerics.tolerantDoubleEquality(0.01)
    assertNetworkValuesWithPredicate[Double]({
      case (0, v) if v === 5.0 => true
      case (1, v) if v === 4.0 => true
      case (2, v) if v === 3.0 => true
      case (3, v) if v === 3.5 => true
      case (4, v) if v === 2.5 => true
      case (5, v) if v === 1.5 => true
      case (6, v) if v === 2.0 => true
      case (7, v) if v === 1.0 => true
      case (8, v) if v === 0.0 => true
      case _ => false
    })()(net)
  }

  EdgeFields should "support broadcasting" in new SimulationContextFixture {
    // ACT
    exec(new TestProgram {
      def broadcast(distance: Int, value: Int) = {
        val dist: EdgeField[Int] = distance
        val loc: EdgeField[(Int,Int)] = pair(dist, value)
        exchange[(Int,Int)](loc)(n =>
          pair(dist,
            // select the `value` exposed by the neighbour with minimal `distance`
            n.fold[(Int,Int)](loc)((t1,t2) => if(t1._1 < t2._1) t1 else t2)._2
          )
        )._2
      }

      override def main(): Int = branch(mid!=1){ broadcast(hopGradient(sense[Boolean](SRC)), mid) }{ -1 }
    }, ntimes = manyRounds)(net)

    // ASSERT
    assertNetworkValues((0 to 8).zip(List(
      8, -1, 8,
      8, 8, 8,
      8, 8, 8
    )).toMap)(net)
  }

  EdgeFields should "support optimised broadcasting" in new SimulationContextFixture {
    exec(new TestProgram {
      override def main(): Int = branch(mid!=1){
        val g = gradient(sense[Boolean](SRC), fsns(nbrRange, Double.PositiveInfinity))
        optimisedBroadcast(g, mid, -1)
      }{ -77 }
    }, ntimes = manyRounds)(net)

    assertNetworkValues((0 to 8).zip(List(
      8, -77, 8,
      8, 8, 8,
      8, 8, 8
    )).toMap)(net)
  }

  /**
    * In this test, we collect 1 for even devices and 0 for odd devices into the sink node (device with ID=8).
    * Therefore, we test that the sink node effectively collects 5 (i.e., counting the contributions
    * of devices with ID = 0, 2, 4, 6, 8)
    */
  EdgeFields should "support multi-path information collection (Cwmp block)" in new SimulationContextFixture {
    exec(new TestProgram {
      def accumulate[T : Numeric](v: EdgeField[T], l: T): T = /* E.g., MAX: implicitly[Builtins.Bounded[T]].max(v,l) */
        v.fold(l)(implicitly[Numeric[T]].plus(_,_))
      def extract(v: Double, w: Double, threshold: Double, Null: Double): Double = //if(w > threshold) v else Null
        v * w

      def weight(dist: Double, radius: Double): EdgeField[Double] = {
        val distDiff: EdgeField[Double] = nbrLocalByExchange(dist).map(v => Math.max(dist-v, 0))
        // println(s"weight: ($radius - ${nbrRangeEF.toLocal}) * (${nbrLocalByExchange(dist)} - $dist)\ndistDiff = $distDiff"+
        // s"R - D(d,d') = ${EdgeField.localToField(radius).map2(nbrRangeEF)(_ - _)}")
        val res = EdgeField.localToField(radius).map2(nbrRangeEF)(_ - _).map2(distDiff)(_ * _)
        // NB: NaN values may arise when `dist`s are Double.PositiveInfinity (e.g., inf - inf = NaN)
        res.map(v => if(v.isNaN) 0 else v)
      }

      def normalize(w: EdgeField[Double]): EdgeField[Double] = {
        val sum: Double = w.fold(0.0)(_+_)
        val res = w.map(_ / sum)
        res.map(v => if(v.isNaN) 0 else v)
      }

      /**
        * Weights corresponding to neighbours are calculated in order to penalise devices that are likely to lose
        *   their “receiving” status, a situation that can happen in two cases:
        * (1) if the “receiving” device is too close to the edge of proximity
        *     of the “sending” device, it might step outside of it in the immediate future breaking the connection;
        * (2) if the potential of the “receiving” device is too close to the potential of the “sending” device,
        *     their relative role of sender/receiver might be switched in the immediate future, possibly
        *     creating an “information loop” between the two devices.
        *  w is positive and symmetric.
        */
      def Cwmp(sink: Boolean, radius: Double, value: Double, Null: Double, threshold: Double = 0.1): Double = {
        var dist = gradient(sink, nbrRangeEF)
        exchange(value)(n => {
          val loc: Double = accumulate(selfSubs(n, 0.0),value)
          val w: EdgeField[Double] = weight(dist, radius)
          val normalized: EdgeField[Double] = normalize(w)
          val res: EdgeField[Double] = normalized.map(extract(loc, _, threshold, Null))
          // println(s"${mid} :: n = $n \n\t loc = $loc \n\t w = $w \n\t normalized = $normalized \n\t res = $res")
          selfSubs(res, loc)
        })
      }

      override def main(): Double = Cwmp(sense[Boolean](SRC), RANGE_RADIUS, if(mid%2==0) 1.0 else 0.0, -1.0)
    }, ntimes = manyRounds)(net)

    implicit val doubleEquality = TolerantNumerics.tolerantDoubleEquality(0.1)
    assertNetworkValuesWithPredicate[Double](
      {
        case (8, v) => v === 5.0
        case _ => true
      }
    )()(net)
  }


  EdgeFields should "support information collection (C block) with subjective reliability estimates" in new SimulationContextFixture {
    // ACT
    exec(new TestProgram {
      override def main(): Double = Csubj[Int,Double](sense[Boolean](SRC), 1, _+_, (v, d) => v*d)
    }, ntimes = manyRounds)(net)

    // ASSERT
    implicit val doubleEquality = TolerantNumerics.tolerantDoubleEquality(0.1)
    assertNetworkValuesWithPredicate[Double](
      {
        case (8, v) => v === 9.0
        case _ => true
      }
    )()(net)
  }


  EdgeFields should "support channels" in new SimulationContextFixture {
    val p = new TestProgram {
      def broadcast[T](dist: Double, value: T): T = {
        val loc = (dist, value)
        exchange[(Double,T)](loc)(n => {
          (dist, n.withoutSelf.fold[(Double, T)](loc)((t1, t2) => if (t1._1 < t2._1) t1 else t2)._2)
        })._2
      }

      def distance(source: Boolean, dest: Boolean): Double = {
        broadcast(distanceTo(source, nbrRangeEF), distanceTo(dest, nbrRangeEF))
      }

      def distanceTo(source: Boolean, metric: EdgeField[Double]): Double =
        exchange(Double.PositiveInfinity)(n =>
          mux(source){ 0.0 } { (n + metric).withoutSelf.fold(Double.PositiveInfinity)(Math.min) }
        )

      def channel(source: Boolean, dest: Boolean, width: Double): Boolean = {
        distanceTo(source, nbrRangeEF) + distanceTo(dest, nbrRangeEF) <= distance(source, dest) + width
      }

      override def main() = branch(sense[Boolean](OBSTACLE)) { -1 } {
        if(channel(sense[Boolean](SRC), mid==0, 0)) 1 else 0
      }
    }

    exec(p, ntimes = manyRounds)(net)

    assertNetworkValues((0 to 8).zip(List(
      1, 1, 0,
      1, 1, -1,
      1, 1, 1
    )).toMap, msg = "Test channel formation")(net)

    net.chgSensorValue(SRC, Set(8), false)
    net.chgSensorValue(SRC, Set(6), true)
    net.chgSensorValue(OBSTACLE, Set(3), true)

    exec(p, ntimes = manyRounds)(net)

    assertNetworkValues((0 to 8).zip(List(
      1, 1, 0,
      -1, 1, -1,
      1, 1, 0
    )).toMap, msg = "Test channel adaptivity")(net)
  }

  private def dropConsecutiveEquals[T](lst: List[T]): List[T] = lst match {
    case a :: b :: tl if a == b => dropConsecutiveEquals(b :: tl)
    case a :: b :: tl => a :: dropConsecutiveEquals(b :: tl)
    case l => l
  }
}
