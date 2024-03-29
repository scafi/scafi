/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.lib

trait StdLibDynamicCode {
  self: StandardLibrary.Subcomponent =>

  trait DynamicCode extends FieldUtils {
    self: FieldCalculusSyntax =>

    /**
      * Mobile functions `fun` should be transferable (i.e., no closures etc), aggregate functions.
      * - If they do note use `aggregate`, they might try to align with functions of other versions,
      *   causing unexpected behaviours and errors.
      */
    final case class Fun[T,R](ver: Int, fun: (T)=>R)

    type Injecter[T,R] = () => Fun[T,R]

    /**
      * Enacts a simple gossip process that supports spreading updated versions of functions.
      * NOTE: different functions running in different parts of the system means that
      * the system is partitioned (by the function identity).
      */
    def up[T,R](injecter: Injecter[T,R]): Fun[T,R] = rep(Fun[T,R](Int.MinValue, _ => ???)){ case f =>
      foldhood(injecter())((f1,f2) => if(f1.ver>=f2.ver) f1 else f2)(nbr{f})
    }

    import Builtins.Bounded

    def exec[T,R:Bounded](procs: List[Fun[T,R]], arg: T, maxVer: Int, curVer: Int, numNbrs: Int, Null: R): (R,Int) = {
      val Fun(headVer,headFun) = procs.head
      // If the version of the head of the list is >= of the min version exec by any neighbour, let's run it
      val curOutcome =
        branch(headVer >= minHood(nbr(curVer))){ headFun.apply(arg) }{ Null }
      // If the version of the head of the list is lower than max, let's recur
      val next =
        branch(headVer < maxVer){ exec(procs.tail, arg, maxVer, curVer, numNbrs, Null) }{ (Null,-1) }
      // If this is the max version and every device has it, let's run it,
      //  otherwise let's return next, i.e., the most up-to-date result
      mux(next._2 < 0 & numNbrs==includingSelf.sumHood(nbr(1))){ (curOutcome, headVer) }{ next }
    }

    def safeUp[T,R:Bounded](injecter: Injecter[T,R], arg: T, Null: R): R =
      rep((List[Fun[T,R]](), -1, -1, Null)) { case (procs, maxVer, curVer, field) =>
        val (newMaxVer, nProcs) = includingSelf.maxHoodSelector(nbr{maxVer})(nbr{(maxVer, procs)})
        val (nnewMaxVer, nnProcs) = branch(injecter().ver > newMaxVer){
          (injecter().ver, nProcs++List(injecter()))
        }{
          (newMaxVer, nProcs)
        }
        val x = exec(nnProcs, arg, nnewMaxVer, curVer, includingSelf.sumHood(nbr(1)), Null)
        (nnProcs, nnewMaxVer, x._2, x._1)
      }._4
  }

}
