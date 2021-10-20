/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.test.functional

import it.unibo.scafi.config.GridSettings
import it.unibo.scafi.test.FunctionalTestIncarnation
import it.unibo.scafi.test.FunctionalTestIncarnation._
import org.apache.commons.math3.random.{RandomDataGenerator, RandomGenerator}

import scala.annotation.tailrec
import scala.collection.Map
import scala.util.Random

case class TestingSeed(seed: Long)

object ScafiTestUtils {

  val (fewRounds, someRounds, manyRounds, manyManyRounds) = (100, 500, 1000, 5000)

  val seeds = List(187372311) //, 204110176, 129995678, 6155814, 22612812, 61168821, 21228945, 146764631, 94412880, 117623077)

  object NetworkDsl {
    case class SensorActivation[T](val name: CNAME, val value: T){
      def inDevices(devs: ID*)(implicit net: Network with SimulatorOps) = net.chgSensorValue(name, devs.toSet, value)
    }
    def setSensor[T](name: CNAME, value: T): SensorActivation[T] = SensorActivation(name, value)
  }

  def partNodes(nodes: Set[ID], net: NetworkSimulator): Map[ID,Set[ID]] = {
    val prev = Map(net.nbrMap.toSeq:_*)
    val nbrs: Map[ID,Set[ID]] = nodes.map(id => id -> detachNode(id, net)).toMap
    nodes.foreach{ id => {
      connectNode(id, nbrs(id).intersect(nodes), net)
    }}
    prev
  }

  def restoreNodes(map: Map[ID,Set[ID]], net: NetworkSimulator) = {
    net.nbrMap.clear()
    map.foreach(net.nbrMap += _)
  }

  def detachNode(id: ID, net: NetworkSimulator): Set[ID] = {
    val nbrs = net.nbrMap(id)
    net.nbrMap(id) = Set()
    nbrs.foreach(nbrId => net.nbrMap(nbrId) -= id)
    nbrs
  }

  def connectNode(id: ID, nbrs: Set[ID], net: NetworkSimulator) = {
    net.nbrMap(id) = nbrs
    nbrs.foreach(nbrId => net.nbrMap(nbrId) += id)
  }

  def runProgram(exp: => Any, ntimes: Int = 500)
                (net: Network with SimulatorOps)
                (implicit node: AggregateInterpreter): (Network, Seq[ID]) ={
    var endNet: Network = null
    val executionSeq =
      net.execMany(
      node = node,
      exp = exp,
      size = ntimes,
      action = (n,i) => {
        if (i % ntimes == 0) {
          endNet = net
        }})
    (endNet, executionSeq)
  }

  def execProgramFor(ap: AggregateProgram, ntimes: Int = 500)
                    (net: Network with SimulatorOps)
                    (when: ID => Boolean, devs: Vector[ID] = net.ids.toVector, rnd: Random = new Random(0)): (Network, Seq[ID]) = {
    var k = ntimes
    var executionSeq = Seq[ID]()
    while(k>0) {
      val nextToRun: ID = until(when) { devs(rnd.nextInt(devs.size)) }
      executionSeq :+= nextToRun
      net.exec(ap, ap.main, nextToRun)
      k -= 1
    }
    (net, executionSeq)
  }

  def exec(ap: AggregateProgram, ntimes: Int = 500)
          (net: Network with SimulatorOps): (Network, Seq[ID]) = {
    runProgram(ap.main(), ntimes)(net)(ap)
  }

  def runProgramInOrder(firingSeq: Seq[ID], ap: AggregateProgram)
                       (net: Network with SimulatorOps): Network ={
    net.execInOrderAndReturn(ap, ap.main(), firingSeq)
  }

  def runProgramInOrder(firingSeq: Seq[ID])
                       (exp: => Any)
                       (net: Network with SimulatorOps)
                       (implicit node: AggregateInterpreter): Network ={
    net.execInOrderAndReturn(node, exp, firingSeq)
  }

  @tailrec
  def until[T](pred: T => Boolean)(expr: => T): T = {
    val res = expr
    if(pred(res)) res else until(pred)(expr)
  }

  def manhattanNet(
                    side: Int = 3,
                    step: Int = 1,
                    rng: Double = 1.5,
                    detachedNodesCoords: Set[(Int, Int)] = Set(),
                    seeds: Seeds = Seeds(newRandomSeed(), newRandomSeed(), newRandomSeed())
                  ): Network with SimulatorOps = {
    var lastDetachedPosition: (Double, Double) = (Int.MaxValue, Int.MaxValue)
    simulatorFactory.gridLike(
      GridSettings(
        side, side, step, step,
        mapPos = (a,b,px,py) =>
          detachedNodesCoords
            .find(_ == (a,b))
            .map{_ =>
              lastDetachedPosition = (lastDetachedPosition._1 - rng, lastDetachedPosition._2 - rng)
              lastDetachedPosition
            }
            .getOrElse[(Double, Double)](px, py)
        ), rng = rng, seeds = seeds)
  }

  def schedulingSequence(ids: Set[ID], sampleSize: Int, minOccurrences: Map[ID,Int] = Map())
                        (implicit seed: TestingSeed = TestingSeed(System.currentTimeMillis())): Seq[ID] = {
    import scala.collection.JavaConverters._
    val rg = new RandomDataGenerator()
    rg.reSeed(seed.seed)
    var seq = Seq[ID]()
    for(_ <- 1 to sampleSize) {
      seq ++= rg.nextSample(ids.asJavaCollection, 1).toSeq.asInstanceOf[Seq[ID]]
    }
    seq
  }

  def newRandomSeed(): Long = System.currentTimeMillis()

  implicit class SchedulingSeq(seq: Seq[ID]) {
    def ensureAtLeast(id: ID, k: Int): Seq[ID] = {
      var outSeq = seq
      val occurrences = seq.count(_ == id)
      if (occurrences < k) {
        val toAdd = k - occurrences
        for (i <- 1 to toAdd) {
          outSeq :+= id
        }
      }
      outSeq
    }

    def ensureLessOrEqualThan(id: ID, k: Int): Seq[ID] = {
      var remaining = k
      seq.filter { v => if (v == id) {
        remaining -= 1
      }; v != id || v == id && remaining >= 0
      }
    }

    def ensureWithin(id: ID, bottom: Int, top: Int): Seq[ID] = {
      ensureAtLeast(id, bottom).ensureLessOrEqualThan(id, top)
    }
  }
}
