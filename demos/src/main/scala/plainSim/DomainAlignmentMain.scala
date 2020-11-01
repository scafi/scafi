/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package plainSim

import it.unibo.scafi.config.GridSettings
import it.unibo.scafi.incarnations.BasicSimulationIncarnation._
import lib.{DomainAlignmentLib}
import sims.SensorDefinitions

/**
  * This program is used to launch simulations on a grid-like network.
  */
object DemoAlignment extends ScafiStandardAggregateProgram with SensorDefinitions with DomainAlignmentLib {
  def isSource = sense[Boolean]("source")

  //def main() = hopGradientByG(isSource) //
  //def main() = (rep(0)(_+1),"%6.2f".format(testGup))
  def main() = (rep(0)(_+1),"%6.2f".format(testGsafe))
  //def main() = (rep(0)(_+1),"%6.2f".format(testCRFup))
  //def main() = (rep(0)(_+1),"%6.2f".format(testCRFsafe))
}


object DomainAlignmentMain extends App {
//  import math._
//  val rand = new Random(0)
//  val idArray = MArray((0 until 1000):_*)
//  val pos = idArray.map(d => d -> (rand.nextInt(100), rand.nextInt(100)) ).toMap
//  val nsnsMap = MMap(NBR_RANGE_NAME ->
//    MMap(idArray.map(d => d -> MMap(idArray.map(d2 => (d2, sqrt(pow(pos(d)._1 - pos(d2)._1,2)+pow(pos(d)._2 - pos(d2)._2,2)) )):_*) ):_*))
//
//  val net = simulatorFactory.basicSimulator(
//    idArray = idArray,
//    nbrMap = MMap(),
//    lsnsMap = MMap(),
//    nsnsMap = nsnsMap
//  )

  val net = simulatorFactory.gridLike(GridSettings(15, 15, stepx = 1, stepy = 1, tolerance = 0.3), rng = 1.5)

  net.addSensor(name = "source", value = false)
  net.chgSensorValue(name = "source", ids = Set(3), value = true)

  var v = java.lang.System.currentTimeMillis()

  net.executeMany(
    node = DemoAlignment,//new HopGradient("source"),
    size = 1000000,
    action = (n,i) => {
      if (i % 1000 == 0) {
        println(net)
        val newv = java.lang.System.currentTimeMillis()
        println(newv-v)
        //println(net.context(4))
        v=newv
      }
    })
}
