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
import it.unibo.scafi.incarnations.BasicAbstractSpatialSimulationIncarnation
import it.unibo.scafi.space.{Point2D, SpaceHelper}

object BasicSpatialIncarnation extends BasicAbstractSpatialSimulationIncarnation {
  override type P = Point2D

  trait MyEuclideanStrategy extends EuclideanStrategy {
    this: Basic3DSpace[_] =>
    override val proximityThreshold = 0.1
  }

  override def buildNewSpace[E](elems: Iterable[(E,P)]): SPACE[E] =
    new Basic3DSpace(elems.toMap) with MyEuclideanStrategy
}

import BasicSpatialIncarnation._

object DemoSpatialLauncher extends App {
  object DemoSpatial extends AggregateProgram {
    def mySensor():Int = sense[Int]("sensor")
    def gradient(source: Boolean): Double = rep(Double.MaxValue){
      distance => mux(source) { 0.0 } {
        minHoodPlus { nbr{distance}+nbrvar[Double](NBR_RANGE_NAME) }
      }
    }
    def main() = foldhood(0)(_+_){1} //gradient(mySensor()==1)
  }

  val (ncols,nrows) = (3,3)
  val (stepx,stepy) = (1,1)
  val positions = SpaceHelper.gridLocations(GridSettings(nrows,ncols,stepx,stepy,tolerance=0))
  val ids = for(i <- 1 to ncols*nrows) yield i
  val devsToPos = ids.zip(positions).toMap
  val net = new SpaceAwareSimulator(
    space = new Basic3DSpace(devsToPos, proximityThreshold = 1.8),
    devs = devsToPos.map { case (d, p) => d -> new DevInfo(d, p,
      lsns => if (lsns == "sensor" && d == 3) 1 else 0,
      nsns => nbr => null)
      },
    simulationSeed = System.currentTimeMillis(),
    randomSensorSeed = System.currentTimeMillis()
    )

  var v = java.lang.System.currentTimeMillis()

  net.executeMany(
    node = DemoSpatial,
    size = 100000,
    action = (n,i) => {
      if (i % 1000 == 0) {
        println(net)
        val newv = java.lang.System.currentTimeMillis()
        println(newv-v)
        v=newv
      }
      if(i>0 && i % 50000 == 0){
        //net.chgSensorValue("sensor", Set(3), 0)
        net.setPosition(3, new Point2D(0,0))
      }
    })
}
