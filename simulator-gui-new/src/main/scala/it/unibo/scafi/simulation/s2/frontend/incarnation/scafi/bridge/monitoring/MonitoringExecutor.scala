/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.monitoring

import it.unibo.scafi.simulation.s2.frontend.controller.logger.LogManager
import it.unibo.scafi.simulation.s2.frontend.controller.logger.LogManager.{Channel, TreeLog}
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.ScafiBridge._
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.ScafiWorldIncarnation._
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.SimulationExecutor

object MonitoringExecutor extends SimulationExecutor {
  override protected def asyncLogicExecution(): Unit = {
    if(contract.simulation.isDefined) {
      val net = contract.simulation.get
      net.exports().filter(_._2.isDefined).map(n => n._1 -> n._2.get).foreach { node =>
        exportProduced += node._1 -> node._2
        if (idsObserved.contains(node._1)) {
          val mapped = node._2.paths.toSeq.map { x => {
            if (x._1.isRoot) {
              (None, x._1, x._2)
            } else {
              (Some(x._1.pull()), x._1, x._2)
            }
          }}.sortWith((x, y) => x._2.level < y._2.level)
          LogManager.notify(TreeLog[Path](Channel.Export, node._1.toString, mapped))
        }
        val metaActions = this.simulationInfo.get.metaActions
        metaActions
          .filter(x => x.valueParser(node._2.root()).isDefined)
          .foreach(x => net.add(x(node._1, node._2)))
        net.process()
      }
    }
  }
}
