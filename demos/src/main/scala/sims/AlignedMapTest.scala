/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package sims
import it.unibo.scafi.incarnations.BasicSimulationIncarnation._
import it.unibo.scafi.simulation.frontend.{Launcher, Settings}

object AlignedMapRunner extends Launcher {
  Settings.Sim_ProgramClass = "sims.AlignedMapTest"
  Settings.ShowConfigPanel = false
  Settings.Sim_NbrRadius = 0.25
  Settings.Sim_NumNodes = 30
  launch()
}

class AlignedMapTest extends AggregateProgram with SensorDefinitions with GradientAlgorithms with BlockG {
  override def main() = test1

  def test1: Set[(Int, String)] = {
    var procs = Map(
      1 -> (()=>f"${distanceTo(sense1)}%.1f"),
      2 -> (()=>f"${distanceTo(sense2)}%.1f"),
      3 -> (()=>f"${distanceTo(sense3)}%.1f"),
      4 -> (()=>f"${distanceTo(sense4)}%.1f"))

    var keyGen = Set[Int]()
    if(sense1) keyGen = keyGen + 1
    if(sense2) keyGen = keyGen + 2
    if(sense3) keyGen = keyGen + 3
    if(sense4) keyGen = keyGen + 4

    var allKeys = rep(keyGen){ myKeys =>
      includingSelf.unionHoodSet(nbr { myKeys ++ keyGen })
    }
    // PROBLEM: how to deal with the removal of processes?

    for(k <- allKeys)
      yield (k -> align(k) { key => procs(key)() })
  }

  def test2: Traversable[String] = {
    // Proc descriptor
    case class Proc[T](generator: () => Boolean, comp: (Boolean) => T)

    // Define processes
    val p1 = Proc(() => sense1, (gen) => f"${distanceTo(gen)}%.1f")
    val p2 = Proc(() => sense2, (gen) => f"${-distanceTo(gen)}%.1f")
    val p3 = Proc(() => sense3, (gen) => f"${distanceToWord(gen)}")
    val p4 = Proc(() => sense4, (gen) => f"${distanceTo(gen)}%.1f")

    // Give PIDs to processes
    val procs = Map(1 -> p1, 2 -> p2, 3 -> p3, 4 -> p4)

    alignedMap(procs.keys, (x: Int) => x, (id: Int) => procs(id).comp(procs(id).generator()))
  }

  /**
    * @param t a collection
    * @param f a function mapping t's values to keys
    * @param g a function computing over t's values
    * @return a collection mapping t's values with g by maintaining alignment over the keys
    */
  def alignedMap[A,K,O](t: Traversable[A], f: A => K, g: A => O): Traversable[O] = {
    t.map(a => {
      val k = f(a)
      align(k){ _ => g(a) }
    })
  }

  def distanceToWord(src: Boolean): String = {
    val dist = distanceTo(src)
    if(dist < 2) "low"
    else if(dist < 10) "med"
    else "hi"
  }

}
