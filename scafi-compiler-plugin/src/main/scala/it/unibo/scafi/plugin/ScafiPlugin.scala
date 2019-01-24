package it.unibo.scafi.plugin

import scala.tools.nsc
import nsc.Global
import nsc.Phase
import nsc.plugins.Plugin
import nsc.plugins.PluginComponent

class ScafiPlugin(val global: Global) extends Plugin {
  import global._

  val name = "divbyzero"
  val description = "checks for division by zero"
  val components = List[PluginComponent](Component)

  private object Component extends PluginComponent {
    val global: ScafiPlugin.this.global.type = ScafiPlugin.this.global
    val runsAfter = List[String]("refchecks")
    val phaseName = ScafiPlugin.this.name
    def newPhase(_prev: Phase) = new DivByZeroPhase(_prev)
    class DivByZeroPhase(prev: Phase) extends StdPhase(prev) {
      override def name = ScafiPlugin.this.name
      def apply(unit: CompilationUnit) {
        //unit.error(unit.body.pos, "just error")
        //unit.warning(unit.body.pos, unit.body.toString())
        /*for ( tree <- unit.body)
        {
          unit.error(tree.pos, "Problem with tree\n" + tree + "\n\n")
        }*/
        object MyTraverser extends Traverser {
          var applies = List[Apply]()
          var withinAggregateClass = false

          override def traverse(tree: global.Tree): Unit = tree match {
            case cd @ ClassDef(_, _, _, Template(parents, selfType, body)) => {
              val ctt = new ClassTypeTraverser("AggregateProgram")
              ctt.traverseTrees(parents)
              withinAggregateClass = ctt.checked
              if(withinAggregateClass) unit.error(cd.pos, "Within aggregate class!!!!!!!!!!!!!!!!!!!!")
              super.traverseTrees(body)
              withinAggregateClass = false
            }
            case Select(This(_),TermName("foldhood")) => unit.error(tree.pos, "Cannot use foldhood")
            case t =>
              super.traverseTrees(t.children)
          }
        }

        class ClassTypeTraverser(val name: String) extends Traverser {
          var checked = false
          override def traverse(tree: global.Tree): Unit = tree match {
            case t => unit.error(t.pos, "ERROR => " + showRaw(t) + " ... with children " + t.children + " ... " + t.freeTerms + " ... " +t.tpe + " ... " + t.toString()); super.traverseTrees(t.children)

            /**
            [error] /..../scafi/demos/src/main/scala/examples/DemoDecentralizedByPropertiesConfig.scala:35:36: ERROR => TypeTree() ... with children List() ... AnyRef ... AnyRef
            [error]   class MyAggregateProgram extends AggregateProgram with Serializable {

              *
            ClassDef(
              Modifiers(ABSTRACT | DEFAULTPARAM/TRAIT),
              examples.DemoCentralizedMain.MyAggregateProgram,
            List(),
            Template(
              List(TypeTree(), TypeTree().setOriginal(
              Select(
                Select(Select(Select(Select(Ident(it), it.unibo), it.unibo.scafi), it.unibo.scafi.incarnations),
                       it.unibo.scafi.incarnations.BasicActorServerBased
                ),
                TypeName("AggregateProgram"))),
              */
          }
        }

        MyTraverser.traverse(unit.body)
      }
    }
  }
}