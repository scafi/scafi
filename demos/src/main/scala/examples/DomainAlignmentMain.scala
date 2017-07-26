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

package examples

import it.unibo.scafi.config.GridSettings
import it.unibo.scafi.incarnations.BasicSimulationIncarnation._
import lib.DomainAlignmentLib
import sims.SensorDefinitions

/**
  * This program is used to launch simulations on a grid-like network.
  */
object DemoAlignment extends AggregateProgram with SensorDefinitions with DomainAlignmentLib {
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
