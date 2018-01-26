/*
 * Copyright (C) 2016-2017, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENCE.txt file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package it.unibo.scafi.test.functional

import it.unibo.scafi.test.FunctionalTestIncarnation._
import org.scalactic.Equality

import scala.collection.Map
import scala.util.Random

object ScafiTestUtils {

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
                (when: ID => Boolean, devs: Vector[ID] = net.ids.toVector, rnd: Random = new Random(0)): Network ={
    if(ntimes <= 0) net
    else{
      val nextToRun = until(when){ devs(rnd.nextInt(devs.size)) }
      net.exec(ap, ap.main, nextToRun)
      execProgramFor(ap, ntimes-1)(net)(when, devs, rnd)
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

}
