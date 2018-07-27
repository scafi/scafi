package it.unibo.scafi.simulation.gui

import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.reflection.Demo
import it.unibo.scafi.simulation.gui.util.ClassFinder

package object demo {
  /**
    * get al demo defined
    */
  lazy val demos : List[Class[_]] = ClassFinder.getClasses("it.unibo.scafi.simulation.gui.demo").filter(x => x.isAnnotationPresent(classOf[Demo]))

  lazy val nameToDemoClass : Map[String,Class[_]] = demos.map(x => x.getSimpleName -> x) toMap
}
