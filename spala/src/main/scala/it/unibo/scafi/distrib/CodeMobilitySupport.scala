/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.distrib

import java.net.{URL, URLClassLoader}

object CodeMobilityUtilities {
  // Apply a generator to create a function with safe decoupled closures
  // See: http://erikerlandson.github.io/blog/2015/03/31/hygienic-closures-for-scala-function-serialization/
  def closureFunction[E,D,R](enclosed: E)(gen: E => (D => R)): D => R = gen(enclosed)

  def log(s: String): Unit = Console.println(s)
}

class CustomClassLoader(parent: ClassLoader) extends URLClassLoader(Array[URL](), parent) {
  import CustomClassLoaderRegistry._
  import CodeMobilityUtilities._

  override protected def findClass(name: String) : Class[_] = {
    log(s"Looking for class ${name}")
    var result = findLoadedClass(name)
    var toreg = classesToRegister.contains(name)
    if (result == null && !toreg) {
      try {
        result = findSystemClass(name)
      } catch {
        case _: Throwable => // Ignore
      }
    }
    if (result == null && toreg) {
      try {
        val classBytes = classesToRegister(name)
        log(s"Defining class ${name}")
        result = defineClass(name, classBytes, 0, classBytes.length)
        save(name)
        log(s"Class ${name} defined!")
      } catch {
        case e: Exception => {
          throw new ClassNotFoundException(name)
        }
      }
    }
    result
  }

  def defineClass(name: String, code: Array[Byte]): Class[_] = {
    try {
      log(s"\nFindin class ${name} with code $code") // TODO: proper loggin
      val klass = this.findLoadedClass(name)
      klass.getClass
    }
    catch {
      case _: Throwable => {
        log("*** defining class") // TODO: proper logging
        this.defineClass(name, code, 0, code.length)
      }
    }
  }
}

object CustomClassLoaderRegistry {
  var classesToRegister: Map[String, Array[Byte]] = Map()
  var registeredClasses: Map[String, Array[Byte]] = Map()

  def register(name: String, classBytes: Array[Byte]): Unit = {
    classesToRegister += (name -> classBytes)
  }

  def save(name: String): Unit = {
    registeredClasses += (name -> classesToRegister(name))
    classesToRegister -= name
  }
}

object LoadClassBytes {
  def apply(clazz: Class[_]) : Map[String, Array[Byte]] = {
    DependencyEmitter.classDependencies(clazz)
      .map(kv => (kv._1.replace('/','.'), kv._2))
      .filter(k => !(k._1.contains("scala.")
        || k._1.contains("java.")
        || k._1.contains("it.unibo.scafi")))
  }
}

import org.apache.bcel.Repository
import org.apache.bcel.classfile._

/**
  * This visitor is responsible of collecting the dependencies of a [[JavaClass]] instance.
  * Adapted from: http://illegalargumentexception.blogspot.it/2008/04/java-finding-binary-class-dependencies.html
  */
class DependencyEmitter(var klass: JavaClass) extends EmptyVisitor {
  var result: Map[String,Array[Byte]] = Map[String,Array[Byte]]()

  override def visitConstantClass(obj: ConstantClass): Unit = {
    val cp = klass.getConstantPool()
    val cname = obj.getBytes(cp)

    // Errors motivated the following try-catch, e.g.,
    //     java.lang.ClassNotFoundException: Exception while looking for class
    //     [Ljava.lang.Object;: java.io.IOException:
    //     Couldn't find: [Ljava/lang/Object;.class
    try {
      result += cname -> Repository.lookupClass(cname).getBytes
    } catch { case _: Throwable => }
  }
}

object DependencyEmitter {
  def classDependencies(clazz: Class[_]): Map[String,Array[Byte]] = {
    val jc: JavaClass = Repository.lookupClass(clazz)
    javaClassDependencies(jc) + (clazz.getName -> jc.getBytes)
  }

  def classDependencies(className: String): Map[String,Array[Byte]] = {
    val jc: JavaClass = Repository.lookupClass(className)
    javaClassDependencies(jc) + (className -> jc.getBytes)
  }

  def javaClassDependencies(jc: JavaClass): Map[String,Array[Byte]] = {
    val visitor = new DependencyEmitter(jc)
    val dv = new DescendingVisitor(jc, visitor)
    dv.visit()
    visitor.result
  }
}
