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

package it.unibo.scafi.lib

trait Stdlib_BlockS {
  self: StandardLibrary.Subcomponent =>

  // scalastyle:off method.name

  import Builtins._

  trait BlockS {
    self: FieldCalculusSyntax with StandardSensors with BlockG =>

    def S(grain: Double, metric: => Double): Boolean =
      breakUsingUids(randomUid, grain, metric)

    def S2(grain: Double): Boolean =
      branch(distanceTo(mid() == minId()) < grain) {
        mid() == minId()
      } {
        S2(grain)
      }

    def minId(): ID = {
      val boundedId = implicitly[Bounded[ID]]
      rep(boundedId.top) { mmid =>
        boundedId.min(mid(), minHood(nbr { mmid }))
      }
    }

    /**
      * Generates a field of random unique identifiers.
      *
      * @return a tuple where the first element is a random number,
      *         end the second element is the device identifier to
      *         ensure uniqueness of the field elements.
      */
    def randomUid: (Double, ID) = rep((Math.random()), mid()) { v =>
      (v._1, mid())
    }

    /**
      * Breaks simmetry using UIDs. UIDs are used to break symmetry
      * by a competition between devices for leadership.
      */
    def breakUsingUids(uid: (Double, ID),
                       grain: Double,
                       metric: => Double): Boolean =
    // Initially, each device is a candidate leader, competing for leadership.
      uid == rep(uid) { lead: (Double, ID) =>
        // Distance from current device (uid) to the current leader (lead).
        val dist = G[Double](uid == lead, 0, (_: Double) + metric, metric)

        // Initially, current device is candidate, so the distance ('dist')
        // will be 0; the same will be for other devices.
        // To solve the conflict, devices abdicate in favor of devices with
        // lowest UID, according to 'distanceCompetition'.
        distanceCompetition(dist, lead, uid, grain, metric)
      }

    /**
      * Candidate leader devices surrender leadership to the lowest nearby UID.
      *
      * @return
      */
    def distanceCompetition(d: Double,
                            lead: (Double, ID),
                            uid: (Double, ID),
                            grain: Double,
                            metric: => Double): (Double, ID) = {
      val inf: (Double, ID) = (Double.PositiveInfinity, uid._2)
      mux(d > grain) {
        // If the current device has a distance to the current candidate leader
        //   which is > grain, then the device candidate itself for another region.
        // Remember: 'grain' represents, in the algorithm,
        //   the mean distance between two leaders.
        uid
      } {
        mux(d >= (0.5 * grain)) {
          // If the current device is at an intermediate distance to the
          //   candidate leader, then it abdicates (by returning 'inf').
          inf
        } {
          // Otherwise, elect the leader with lowest UID.
          // Note: it works because Tuple2 has an OrderingFoldable where
          //   the min(t1,t2) is defined according the 1st element, or
          //   according to the 2nd elem in case of breakeven on the first one.
          //   (minHood uses min to select the candidate leader tuple)
          minHood {
            mux(nbr { d } + metric >= 0.5 * grain) {
              nbr { inf }
            } {
              nbr { lead }
            }
          }
        }
      }
    }
  }

}
