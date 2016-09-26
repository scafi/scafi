package examples

import it.unibo.scafi.incarnations.BasicSimulationIncarnation._
import it.unibo.scafi.incarnations.BasicSimulationIncarnation.Builtins._

trait MyLib { self: Constructs with Builtins =>
  def nbrRange():Double = nbrvar[Double](NBR_RANGE_NAME)

  /**
   * Gradient cast.
   * @param source represents the source field. Locally, it indicates if
   *               the device is a source for the gradientcast.
   * @param field represents the field of initial values.
   * @param acc is the accumulation function applied along the gradient.
   * @param metric represents the notion of distance.
   * @tparam V is the type of the "points" of the value field.
   * @return globally, a field of values calculated by accumulation along
   *         the gradient; locally, the accumulated value for each device.
   */
  def G[V: OrderingFoldable](source: Boolean, field: V, acc: V => V, metric: => Double): V =
    rep( (Double.MaxValue, field) ){ dv =>
      mux(source) {
        (0.0, field)
      } {
        minHoodPlus {
          val (d, v) = nbr { (dv._1, dv._2) }
          (d + metric, acc(v)) }
      }}._2

  def distanceTo(source:Boolean): Double =
    G[Double](source,0, _ + nbrRange(), nbrRange())

  def broadcast[V: OrderingFoldable](source:Boolean, field: V):V =
    G[V](source,field, x=>x , nbrRange())

  def distanceBetween(source:Boolean, target:Boolean):Double =
    broadcast(source, distanceTo(target))

  def channel(source:Boolean, target:Boolean, width:Double): Boolean =
    distanceTo(source) + distanceTo(target) <=
      distanceBetween(source,target) + width

  def gradientByG(src: Boolean): Double = G(src, 0.0, (x:Double)=>x+nbrRange(), nbrRange())
  def hopGradientByG(src: Boolean): Int = G(src, 0, (x:Int)=>x+1, 1)

  /**
   * @return the ID of the neighbour whose 'potential' value is minimum
   *         among the neighbours.
   */
  def findParent[V](potential: V)(implicit ev: OrderingFoldable[V]): ID = {
    mux(ev.compare(minHood{ nbr(potential) }, potential)<0 ){
      minHood{ nbr{ Tuple2[V,ID](potential, mid()) } }._2
    }{
      Int.MaxValue
    }
  }

  /** accumulate-hood uses the function in its first argument
    * to combine values from the field in its second argument,
    */
  def accumulateHood[V: OrderingFoldable](acc:(V,V)=>V)(field:V): V = {
    acc(field, field)
  }

  /**
   * @param potential is the potential field up which the values
   *                  of local should be accumulated.
   * @param acc must be a commutative and associative function of two arguments;
   *            it is used to combine values along the potential field.
   * @param local
   * @param Null does not affect the accumulated value; it is used to ignore
   *             neighbours in ordered to avoid multiply-counting devices
   *             (for those accumulations that are not idempotent).
   * @tparam V type of the potential field and the accumulated value.
   * @return the final result of the accumulation along the potential field.
   */
  def C[V: OrderingFoldable](potential: V, acc: (V,V)=>V, local:V, Null:V): V = {
    rep(local){ v =>
      acc(local, foldhood(Null)(acc){
        mux(nbr(findParent(potential)) == mid()){
          nbr(v)
        } {
          nbr(Null)
        }
      })
    }
  }

  def summarize(sink: Boolean, acc:(Double,Double)=>Double, local:Double, Null:Double): Double =
    broadcast(sink, C(distanceTo(sink), acc, local, Null))

  def average(sink: Boolean, value: Double): Double =
    summarize(sink, (a,b)=>{a+b}, value, 0.0) / summarize(sink, (a,b)=>a+b, 1, 0.0)

  def T[V](initial: V, floor: V, decay: V=>V)
          (implicit ev: Numeric[V]): V = {
    rep(initial){ v =>
      ev.min(initial, ev.max(floor, decay(v)))
    }
  }

  def T[V](initial: V, decay: V=>V)
          (implicit ev: Numeric[V]): V = {
    T(initial, ev.zero, decay)
  }

  def T[V](initial: V)
          (implicit ev: Numeric[V]): V = {
    T(initial, (t:V)=>ev.minus(t, ev.one))
  }

  def timer[V](length: V)
              (implicit ev: Numeric[V]) =
    T[V](length)

  def limitedMemory[V,T](value: V, expValue: V, timeout: T)
                        (implicit ev: Numeric[T]) = {
    val t = timer[T](timeout)
    (if(ev.gt(t, ev.zero)) value else expValue, t)
  }

  /**
   * Sparse spatial choice.
   * Refreshing the device ID each round ensures this is self-stabilising.
   * These UIDs are then used to break symmetry by a competition
   * between devices for leadership: candidate leader devices surrender
   * leadership to the lowest nearby UID (measuring distance with metric
   * and G). In the case where no device nearby is a leader,
   * devices nominate themselves.
   * @param grain Represents the mean distance between two leaders.
   * @param metric Represents the notion of 'distance' (usually 1 or nbr-range).
   * @return true if the node has been elected leader, false otherwise.
   */
  def S(grain: Double,
        metric: => Double): Boolean =
    breakUsingUids(randomUid, grain, metric)

  def minId(): ID = {
    rep(Int.MaxValue){ mmid => math.min(mid(), minHood(nbr { mmid })) }
  }

  def S2(grain: Double): Boolean =
    branch( distanceTo(mid()==minId())<grain ){
      mid() == minId()
    }{
      S2(grain)
    }

  /**
   * Generates a field of random unique identifiers.
   * @return a tuple where the first element is a random number,
   *         end the second element is the device identifier to
   *         ensure uniqueness of the field elements.
   */
  def randomUid: (Double,ID) = rep((Math.random()), mid()) { v =>
    (v._1, mid())
  }

  /**
   * Breaks simmetry using UIDs. UIDs are used to break symmetry
   * by a competition between devices for leadership.
   */
  def breakUsingUids(uid:(Double,ID),
                     grain:Double,
                     metric: => Double): Boolean =
    // Initially, each device is a candidate leader, competing for leadership.
    uid == rep(uid) { lead:(Double,ID) =>
      // Distance from current device (uid) to the current leader (lead).
      val dist = G[Double](uid==lead, 0, (_:Double)+metric, metric)

      // Initially, current device is candidate, so the distance ('dist')
      // will be 0; the same will be for other devices.
      // To solve the conflict, devices abdicate in favor of devices with
      // lowest UID, according to 'distanceCompetition'.
      distanceCompetition(dist, lead, uid, grain, metric)
    }

  /**
   * Candidate leader devices surrender leadership to the lowest nearby UID.
   * @return
   */
  def distanceCompetition(d: Double,
                          lead:(Double,ID),
                          uid:(Double,ID),
                          grain:Double,
                          metric: => Double) = {
    val inf:(Double,ID) = (Double.PositiveInfinity, uid._2)
    mux(d > grain){
      // If the current device has a distance to the current candidate leader
      //   which is > grain, then the device candidate itself for another region.
      // Remember: 'grain' represents, in the algorithm,
      //   the mean distance between two leaders.
      uid
    }{
      mux(d >= (0.5*grain)){
        // If the current device is at an intermediate distance to the
        //   candidate leader, then it abdicates (by returning 'inf').
        inf
      }{
        // Otherwise, elect the leader with lowest UID.
        // Note: it works because Tuple2 has an OrderingFoldable where
        //   the min(t1,t2) is defined according the 1st element, or
        //   according to the 2nd elem in case of breakeven on the first one.
        //   (minHood uses min to select the candidate leader tuple)
        minHood {
          mux(nbr{d}+metric >= 0.5*grain){ nbr{inf} }{ nbr{lead} }
        }
      }
    }
  }

  /* Restriction in space */

  def distanceAvoidingObstacles(src: Boolean, obstacle: Boolean): Double =
    branch(obstacle){Double.PositiveInfinity}{distanceTo(src)}

  def broadcastRegion[V:OrderingFoldable](region: Boolean, src: Boolean, value: V): Option[V] =
    branch[Option[V]](region){ Some[V](broadcast(src, value)) }{ None }

  def groupSize(region: Boolean): Double =
    branch(region){ summarize(S(1,0), _+_, 1, 0) } { Double.NaN }

  def recentEvent(event: Boolean, timeout: Int): Boolean =
    branch(event){ true } { timer(timeout)>0 }


  def field[A](expr: => A): List[A] = foldhood(List[A]())(_++_){ List[A](expr) }
  def devField[A](expr: => A): Map[ID,A] = foldhood(Map[ID,A]())(_++_){ nbr { Map[ID,A](mid()->expr) } }

  /* Aligned map */

  def alignedMap[K,V,T](map: Map[K,V], f:V=>T): Map[K,T] = {
    val maps = devField { map }
    val keys = scala.collection.mutable.Map[K,List[ID]]()

    maps.foreach{ devMaps =>
      devMaps._2.foreach { tp =>
        keys += tp._1 -> (devMaps._1 :: keys.getOrElse(tp._1,List[ID]()))
      }
    }

    val res = scala.collection.mutable.Map[K,T]()
    map.foreach { k =>
      res += k._1 -> devField(f(k._2))(mid())
    }
    res.toMap
  }

  def alignedMap2[K,V,T](map: Map[K,V], f:V=>T): Map[K,T] = {
    map.map(tp => tp._1 -> f(tp._2))
  }

  /***********************************/
  /* IEEE Computer: Crowd estimation */
  /***********************************/

  val (high,low,none) = (2,1,0) // crowd level
  def managementRegions(grain: Double, metric: => Double): Boolean =
    S(grain, metric) /*{
    breakUsingUids(randomUid, grain, metric)
  }*/

  def unionHoodPlus[A](expr: => A): List[A] =
    foldhoodPlus(List[A]())(_++_){ List[A](expr) }

  def densityEst(p: Double, range: Double): Double = {
    val nearby = unionHoodPlus(
      mux (nbrRange < range) { nbr(List(mid())) } { List() }
    )

    val footprint = 1 /*if(self.hasEnvironmentVariable("footprint")) {
      self.getEnvironmentVariable("footprint") } else { 1 }*/
    nearby.size / p / (Math.PI * Math.pow(range,2) * footprint)
  }

  def rtSub(started: Boolean, state: Boolean, memoryTime: Double): Boolean = {
    if(state) {
      true
    } else {
      limitedMemory[Boolean,Double](started, false, memoryTime)._1
    }
  }

  def recentTrue(state: Boolean, memoryTime: Double): Boolean = {
    rtSub(timer(10) == 0, state, memoryTime)
  }

  def dangerousDensity(p: Double, r: Double) = {
    val mr = managementRegions(r*2, nbrRange)
    val danger = average(mr, densityEst(p, r)) > 2.17 &&
      summarize(mr, (_:Double)+(_:Double), 1 / p, 0) > 300
    if(danger) { high } else { low }
  }

  def crowdTracking(p: Double, r: Double, t: Double) = {
    val crowdRgn = recentTrue(densityEst(p, r)>1.08, t)
    if(crowdRgn) { dangerousDensity(p, r) } else { none }
  }

  /**
   *
   * @param p estimates the proportion of people with a device running the app
   * @param r is the range in which the neighbours are counted
   * @param warn estimates fraction of walkable space in the local urban env
   * @param t is the memory time
   * @return a boolean indicating whether there is warning or not.
   */
  def crowdWarning(p: Double, r: Double, warn: Double, t: Double): Boolean = {
    distanceTo(crowdTracking(p,r,t) == high) < warn
  }

  /*****************************************/
  /* FORTE15: Code Mobility Meets Self-Org */
  /*****************************************/

  def snsInjectedFun: ()=>Double = forte15logic //sense("injectedFun")
  def snsSource: Boolean = sense("source")
  def snsInjectionPoint: Boolean = sense("injectionPoint")
  def snsPatron: Double = if(sense("patron")){ 1 } else { 0 }
  // ;; Simple low-pass filter for smoothing noisy signal ’value’ with rate constant ’alpha’
  def lowPass(alpha: Double, value: Double): Double = {
    rep(value){ filtered =>
      (value * alpha) + (filtered * (1 - alpha))
    }
  }

  //;; Evaluate a function field, running ’f’ from ’source’ within ’range’ meters, and ’no-op’ elsewhere
    def deploy[T](range:Double, source:Boolean, g: ()=>T, noOp: ()=>T)
                 (implicit ev: OrderingFoldable[T]): T = {
      val f: ()=>T = if (distanceTo(source) < range) {
        G(source, g, identity[()=>T], nbrRange())
      } else {
        noOp
      }
      f()
    }

  /**
   * The entry-point function executed to run the virtual machine on each device.
   * @return
   */
  def virtualMachine(): Double = {
    deploy(nbrRange, snsInjectionPoint, snsInjectedFun, ()=>0.0)
  }

  def forte15logic() = lowPass(alpha = 0.5,
            value = C[Double](potential = distanceTo(snsInjectionPoint),
            acc = _+_, local = snsPatron, Null = 0.0))

  def forte15example() = {
    virtualMachine()
  }
}