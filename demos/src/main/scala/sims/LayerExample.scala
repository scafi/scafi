/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package sims

import it.unibo.scafi.incarnations.BasicSimulationIncarnation._
import it.unibo.scafi.simulation.frontend.{Launcher, Settings}

object LayerExampleMain extends Launcher {
  // Configuring simulation
  Settings.Sim_ProgramClass = "sims.LayerExampleProgram" // starting class, via Reflection
  Settings.ShowConfigPanel = false // show a configuration panel at startup
  Settings.Sim_NbrRadius = 0.15 // neighbourhood radius
  Settings.Sim_NumNodes = 100 // number of nodes
  launch()
}

trait Layers { self: ScafiStandardAggregateProgram =>
  class LayerSpec(var variations: Seq[Variation] = Seq.empty) {
    def befores = variations.filter(_.kind==Before)
    def afters = variations.filter(_.kind==After)
    def replacements = variations.filter(_.kind==Replace)
  }

  trait VariationKind
  object After extends VariationKind
  object Before extends VariationKind
  object Replace extends VariationKind

  trait VariationSignal
  object Proceed extends VariationSignal
  object Stop extends VariationSignal

  case class Variation(name: String, kind: VariationKind, behaviour: ()=>VariationOutput[Any])
  trait LayerBuilder {
    def before[T](method: String)(behaviour: => VariationOutput[T]): LayerBuilder
    def after[T](method: String)(behaviour: => VariationOutput[T]): LayerBuilder
    def replace[T](method: String)(behaviour: => VariationOutput[T]): LayerBuilder
    val layerSpec: LayerSpec
  }

  case class VariationOutput[+T](result: T, signal: VariationSignal)
  object VariationOutput {
    implicit def conv[T](tp: (T,VariationSignal)): VariationOutput[T] = VariationOutput(tp._1, tp._2)
  }

  object LayerBuilder {
    implicit def builderToSpec(builder: LayerBuilder): LayerSpec = builder.layerSpec
  }
  class LayerBuilderImpl(val layerSpec: LayerSpec = new LayerSpec()) extends LayerBuilder {
    override def before[T](method: String)(behaviour: => VariationOutput[T]): LayerBuilder =
      new LayerBuilderImpl(new LayerSpec(layerSpec.variations :+ Variation(method, Before, ()=>behaviour)))

    override def after[T](method: String)(behaviour: => VariationOutput[T]): LayerBuilder =
      new LayerBuilderImpl(new LayerSpec(layerSpec.variations :+ Variation(method, After, ()=>behaviour)))

    override def replace[T](method: String)(behaviour: => VariationOutput[T]): LayerBuilder =
      new LayerBuilderImpl(new LayerSpec(layerSpec.variations :+ Variation(method, Replace, ()=>behaviour)))
  }

  case class Layer(name: String, cond: ()=>Boolean, spec: LayerSpec)

  var layers: Set[Layer] = Set.empty
  def activeLayers: Set[Layer] = layers.filter(l => align("active"+l.name){ _ => l.cond() })
  var layeredMethods: Map[String,()=>Any] = Map.empty

  def layer(name: String)
           (layerSpec: LayerBuilder => LayerSpec)
           (when: => Boolean) =
    layers += Layer(name, ()=>when, layerSpec(new LayerBuilderImpl()))

  def layered[T](name: String)(expr: => T): T = try {
    activeLayers.foreach(l =>
      l.spec.befores.filter(_.name == name).foreach(v => align(v){ _ => v.behaviour() })
    )
    (for(l <- activeLayers.toList;
        v <- l.spec.replacements) yield align(v){_ => v.behaviour() }.result.asInstanceOf[T]).lastOption.getOrElse(align(s"layeres$name" ){ _ => expr })
  } finally {
    activeLayers.foreach(l =>
      l.spec.afters.filter(_.name == name).foreach(v => align(v){ _ => v.behaviour() })
    )
  }
}

class LayerExampleProgram extends ScafiStandardAggregateProgram with SensorDefinitions with Layers with BlockS {
  def lowPower = sense1

  object L { // Layers
    val LowPower = "lowPower"
  }

  object M { // Layered methods
    val LeaderElection = "leaderElection"
  }

  var k = 0

  layer(L.LowPower){_
      .before(M.LeaderElection){
        k = rep(0)(_+1)
        () -> Proceed
      }
      .replace[Boolean](M.LeaderElection){
        S(20, nbrRange) -> Stop
      }
  }(when = lowPower)

  def leaderElection() : Boolean = layered(M.LeaderElection){
    S(100, nbrRange)
  }

  override def main() = {
    val leaders = leaderElection()
    (leaders,k)
  }
}

