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

package it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.actor

import it.unibo.scafi.simulation.gui.controller.logger.LogManager
import it.unibo.scafi.simulation.gui.controller.logger.LogManager.{Channel, TreeLog}
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiWorldIncarnation.{EXPORT, ID}
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.scafiSimulationExecutor.{contract, indexToName, simulationInfo, simulationObserver, world, _}

import scala.concurrent.duration._
import akka.util.Timeout
import akka.pattern.ask
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.actor.PlatformSimulatorActor.{MsgExports, MsgGetExports, MsgNeighborhood}

import scala.language.postfixOps
import scala.concurrent.Await

object ActorPlatformSimulationExecutor extends ActorPlatformBridge {
  import ActorPlatformBridge._
  private var exports: Map[ID, EXPORT] = Map()
  private val indexToName = (i : Int) => "output" + (i + 1)
  //override val minDelta: Int = 1000
  override protected val maxDelta: Option[Int] = None

  import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiWorldIncarnation._
  var space: Option[QuadTreeSpace[ID]] = None

  override def asyncLogicExecution(): Unit = {
    contract.simulation.foreach { sim =>
      implicit val timeout: Timeout = Timeout(1 seconds)
      Await.result(sim ? MsgGetExports(), timeout.duration).asInstanceOf[MsgExports] match {
        case MsgExports(exps) =>
          exports = exps
          exports.foreach { node =>
            if (idsObserved.contains(node._1)) {
              val mapped = node._2.paths.toSeq.map { x => {
                if (x._1.isRoot) {
                  (None, x._1,x._2)
                } else {
                  (Some(x._1.pull()), x._1, x._2)
                }
              }}.sortWith((x,y) => x._2.level < y._2.level)
              LogManager.notify(TreeLog[Path](Channel.Export, node._1.toString, mapped))
            }
            /*val metaActions = this.simulationInfo.get.metaActions
            metaActions.filter(x => x.valueParser(node._2.root()).isDefined).foreach(x => net.add(x(node._1, node._2)))
            net.process()*/
          }
        case _ =>
      }
      //world.nodes.foreach(n => sim ! MsgNeighborhood(n.id, space.get.getNeighbors(n.id).toSet))
    }
  }

  override def onTick(float: Float): Unit = {()
    val simulationMoved = simulationObserver.idMoved
    if(contract.simulation.isDefined) {
      val bridge = contract.simulation.get
      val exportEvaluations = simulationInfo.get.exportValutations
      if(exportEvaluations.nonEmpty) {
        var exportToUpdate: Map[ID, EXPORT] = Map()
        exportToUpdate = exports
        exports = Map()
        for (export <- exportToUpdate) {
          for (i <- exportEvaluations.indices) {
            world.changeSensorValue(export._1, indexToName(i), exportEvaluations(i)(export._2))
          }
        }
      }
      var idsNetworkUpdate: Set[ID] = Set()
      simulationMoved foreach {id =>
        world.moveNode(id, space.get.getLocation(id))
        idsNetworkUpdate ++= world.network.neighbours(id)
        idsNetworkUpdate ++= space.get.getNeighbors(id)
        idsNetworkUpdate += id
      }
      idsNetworkUpdate foreach { x => { world.network.setNeighbours(x, space.get.getNeighbors(x).toSet) } }

      /*val simulationSensor = simulationObserver.idSensorChanged
      simulationSensor.foreach(nodeChanged => nodeChanged._2.foreach(name =>
        world.changeSensorValue(nodeChanged._1, name, bridge.localSensor(name)(nodeChanged._1))))*/

    }
  }
}

