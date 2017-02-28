package lib

/**
 * @author Roberto Casadei
 *
 */

import it.unibo.scafi.incarnations.BasicSimulationIncarnation._

trait DomainAlignmentLib { self: AggregateProgram with SensorDefinitions =>

  //def nbrRange():Double = nbrvar[Double](NBR_RANGE_NAME)
  def time = rep(0)(_+1)
  val inf = Double.PositiveInfinity

  ///////////////////////////////////////
  // domain-aligned updatable function //
  ///////////////////////////////////////

  /**
    * @param ver Version of the function
    * @param fun Function
    */
  case class VersionedFunction(ver: Int, fun: ()=>Double) {
    def max(o: VersionedFunction) = {
      if (ver > o.ver) this else o
    }
  }

  /**
    * Executes every function in list 'procs' which is not older than any version currently used by a neighbour.
    * @return the outcome of the function with the highest version number that is shared by all neighbours
    */
  def exec(procs: List[VersionedFunction], maxp: Int, curp: Int, nnum: Int, init: ()=>Double): (Double,Int) = aggregate{
    val d_cur = branch (minHood(nbr{curp}) <= procs.head.ver) {procs.head.fun()} {init()}
    val x: (Double,Int) = branch (procs.head.ver < maxp) {
      exec(procs.tail, maxp, curp, nnum, init)
    } { (init(),-1) }
    val d_nxt = x._1
    val ncurp:Int = x._2
    mux(ncurp<0 && nnum == foldhood(0)(_+_){1}) { (d_cur, procs.head.ver) } { x }
  }

  /**
    * procs: list of functions ever executed on the network
    * maxp: the max version number in procs
    * curp: the version number of the function used in previous round
    * field: the output field
    *
    * @param metric The "injecter" of the functions.
    * @param init Initial version of the function.
    * @return
    */
  def safeup(metric: ()=>VersionedFunction, init: ()=>Double) = aggregate{
    (rep ((List[VersionedFunction](), -1, -1, nbr{0.0})) {
      case ((procs:List[VersionedFunction], maxp:Int, curp:Int, field:Double)) => aggregate {
        val y: (Int,List[VersionedFunction]) = foldhood((maxp,procs))( (a,b) => if(a._1>=b._1) a else b){ nbr{(maxp, procs)} }
        val z: (Int,List[VersionedFunction]) = mux(metric().ver > maxp){ (metric().ver, metric() :: procs) }{ y }
        val nmaxp = z._1
        val nprocs = z._2
        val x = exec(nprocs, nmaxp, curp, foldhood(0)(_+_){1}, init)
        (nprocs, nmaxp, x._2, x._1)
      }
    })._4
  }

  ////////////////////////////////
  // trivial updatable function //
  ////////////////////////////////

  def up(f: ()=>VersionedFunction) = aggregate{
    (rep(f()) {
      (x) => aggregate{foldhood(f())(_.max(_)){nbr{x}}}
    } ).fun
  }

  ////////////////////////////////////
  // trivial and adaptive gradients //
  ////////////////////////////////////

  case class RaisingDist(dist: Double, raising: Boolean) {
    def +(delta: Double) = {
      RaisingDist(dist+delta, raising)
    }
    def min(o: RaisingDist) = {
      if (raising == o.raising) {
        if (dist < o.dist) this else o
      } else {
        if (raising) o else this
      }
    }
  }

  def crfgradient(source: Boolean, metric: ()=>Double) = aggregate{
    (rep (RaisingDist(inf,false)) {
      (d) => aggregate{
        val x = foldhood(RaisingDist(inf,false))(_.min(_)){nbr{d} + nbrRange()}
        RaisingDist(x.dist, x.dist > d.dist || x.raising)
      }
    }).dist
  }

  def gradient(source: Boolean, metric: ()=>Double) = aggregate{
    rep (inf) {
      (dist) => mux(source){ 0.0 } { foldhood(inf)(Math.min(_,_))(metric()+nbr{dist}) }
    }
  }

  ////////////////////////////
  // metrics to be injected //
  ////////////////////////////

  def badmetric(): Double = aggregate{
    nbrRange() * (4.5+Math.random()) * 0.2
  }

  def smartmetric(): Double = aggregate{
    nbrRange() * foldhood(0)(_+_){1}
  }

  def injmetric(): VersionedFunction = aggregate{
    if (mid() == 1 && time < 50){ /*print('a');*/ VersionedFunction(2, nbrRange) }
    else if (mid() == 2 && time > 50 && time < 100) { /*print('z');*/ VersionedFunction(3, smartmetric) }
    else VersionedFunction(1, badmetric)
  }

  ////////////////////
  // selected tests //
  ////////////////////

  def testGup() = {
    gradient(mid() == 0, () => aggregate{up(()=>injmetric)()})
  }

  def testGsafe() = {
    gradient(mid() == 0, () => aggregate{safeup(()=>injmetric,()=>badmetric)})
  }

  def testCRFup() = {
    crfgradient(mid() == 0, () => aggregate{up(()=>injmetric)()})
  }

  def testCRFsafe() = {
    crfgradient(mid() == 0, () => aggregate{safeup(()=>injmetric,()=>badmetric)})
  }
}
