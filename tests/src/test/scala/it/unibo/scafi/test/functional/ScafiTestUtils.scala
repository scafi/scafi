/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.test.functional

import it.unibo.scafi.config.GridSettings
import it.unibo.scafi.test.FunctionalTestIncarnation
import it.unibo.scafi.test.FunctionalTestIncarnation._

import scala.collection.Map
import scala.util.Random

object ScafiTestUtils {

  val (fewRounds, someRounds, manyRounds, manyManyRounds) = (100, 500, 1000, 2000)

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
                (implicit node: AggregateInterpreter): Network ={
    var endNet: Network = null
    net.execMany(
      node = node,
      exp = exp,
      size = ntimes,
      action = (n,i) => {
        if (i % ntimes == 0) {
          endNet = net
        }})
    endNet
  }

  def execProgramFor(ap: AggregateProgram, ntimes: Int = 500)
                    (net: Network with SimulatorOps)
                    (when: ID => Boolean, devs: Vector[ID] = net.ids.toVector, rnd: Random = new Random(0)): Network = {
    if (ntimes <= 0) net
    else {
      val nextToRun = until(when) {
        devs(rnd.nextInt(devs.size))
      }
      net.exec(ap, ap.main, nextToRun)
      execProgramFor(ap, ntimes - 1)(net)(when, devs, rnd)
    }
  }

  def exec(ap: AggregateProgram, ntimes: Int = 500)
          (net: Network with SimulatorOps): Network = {
    runProgram(ap.main(), ntimes)(net)(ap)
  }

  def runProgramInOrder(firingSeq: Seq[ID])
                       (exp: => Any)
                       (net: Network with SimulatorOps)
                       (implicit node: AggregateInterpreter): Network ={
    net.execInOrderAndReturn(node, exp, firingSeq)
  }

  def until[T](pred: T => Boolean)(expr: => T): T = {
    val res = expr
    if(pred(res)) res else until(pred)(expr)
  }

  def manhattanNet(
                    side: Int = 3,
                    step: Int = 1,
                    rng: Double = 1.5,
                    detachedNodesCoords: Set[(Int, Int)] = Set()
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
        ), rng = rng)
  }
}
