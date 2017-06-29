package lib

import it.unibo.scafi.incarnations.BasicSimulationIncarnation._

/**
  * @author Roberto Casadei
  *
  */
trait BlockS { self: AggregateProgram with SensorDefinitions with BlockG =>
  def S(grain: Double, metric: => Double): Boolean =
    breakUsingUids(randomUid, grain, metric)

  def S2(grain: Double): Boolean =
    branch(distanceTo(mid() == minId()) < grain) {
      mid() == minId()
    } {
      S2(grain)
    }

  def minId(): ID = {
    rep(Int.MaxValue) { mmid =>
      math.min(mid(), minHood(nbr { mmid }))
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
                          metric: => Double) = {
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
        minHood { mux(nbr { d } + metric >= 0.5 * grain){
          nbr { inf }
        }{
          nbr { lead }
        } }
      }
    }
  }
}
