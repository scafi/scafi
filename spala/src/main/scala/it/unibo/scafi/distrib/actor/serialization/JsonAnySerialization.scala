/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.distrib.actor.serialization

import it.unibo.scafi.distrib.Platform
import it.unibo.scafi.space.{Point2D, Point3D}
import org.slf4j.LoggerFactory
import play.api.libs.json.{JsArray, JsValue, Json}

// scalastyle:off cyclomatic.complexity

trait JsonAnySerialization {
  def anyToJs: PartialFunction[Any, JsValue]
  def jsToAny: PartialFunction[JsValue, Any]
}

trait BasicJsonAnySerialization extends JsonAnySerialization
  with JsonPrimitiveSerialization
  with JsonOptionSerialization
  with JsonCollectionSerialization
  with JsonTupleSerialization
  with JsonCommonFunctionSerialization {

  override def anyToJs: PartialFunction[Any, JsValue] =
    super[JsonPrimitiveSerialization].anyToJs orElse
    super[JsonOptionSerialization].anyToJs orElse
    super[JsonCollectionSerialization].anyToJs orElse
    super[JsonTupleSerialization].anyToJs orElse
    super[JsonCommonFunctionSerialization].anyToJs

  override def jsToAny: PartialFunction[JsValue, Any] =
    super[JsonPrimitiveSerialization].jsToAny orElse
    super[JsonOptionSerialization].jsToAny orElse
    super[JsonCollectionSerialization].jsToAny orElse
    super[JsonTupleSerialization].jsToAny orElse
    super[JsonCommonFunctionSerialization].jsToAny
}

trait JsonPrimitiveSerialization extends JsonAnySerialization {
  override def anyToJs: PartialFunction[Any, JsValue] = {
    case b:Boolean => Json.obj("type" -> "Boolean", "val" -> b)
    case b:Byte => Json.obj("type" -> "Byte", "val" -> b)
    case i:Int => Json.obj("type" -> "Int", "val" -> i)
    case l:Long => Json.obj("type" -> "Long", "val" -> l)
    case f:Float => Json.obj("type" -> "Float", "val" -> f)
    case Double.PositiveInfinity => Json.obj("type" -> "DoublePositiveInfinity")
    case d:Double => Json.obj("type" -> "Double", "val" -> d)
    case c:Char => Json.obj("type" -> "Char", "val" -> c.toString)
    case s:String => Json.obj("type" -> "String", "val" -> s)
    case c: Class[_] => Json.obj("type" -> "Class", "name" -> c.getName)
    case pt:Point2D => Json.obj("type" -> "Point2D", "x" -> pt.x, "y" -> pt.y)
    case pt:Point3D => Json.obj("type" -> "Point3D", "x" -> pt.x, "y" -> pt.y, "z" -> pt.z)
  }
  override def jsToAny: PartialFunction[JsValue, Any] = {
    case b if (b \ "type").as[String] == "Boolean" => (b \ "val").as[Boolean]
    case b if (b \ "type").as[String] == "Byte" => (b \ "val").as[Byte]
    case i if (i \ "type").as[String] == "Int" => (i \ "val").as[Int]
    case l if (l \ "type").as[String] == "Long" => (l \ "val").as[Long]
    case f if (f \ "type").as[String] == "Float" => (f \ "val").as[Float]
    case i if (i \ "type").as[String] == "DoublePositiveInfinity" => Double.PositiveInfinity
    case d if (d \ "type").as[String] == "Double" => (d \ "val").as[Double]
    case c if (c \ "type").as[String] == "Char" => (c \ "val").as[String].head
    case s if (s \ "type").as[String] == "String" => (s \ "val").as[String]
    case c if (c \ "type").as[String] == "Class" => Class.forName((c \ "name").as[String])
    case p if (p \ "type").as[String] == "Point2D" => Point2D((p \ "x").as[Double], (p \ "y").as[Double])
    case p if (p \ "type").as[String] == "Point3D" => Point3D((p \ "x").as[Double], (p \ "y").as[Double], (p \ "z").as[Double])
  }
}

trait JsonOptionSerialization extends JsonAnySerialization {
  override def anyToJs: PartialFunction[Any, JsValue] = {
    case Some(o) => Json.obj("type" -> "Option", "isDefined" -> true, "val" -> anyToJs(o))
    case None => Json.obj("type" -> "Option", "isDefined" -> false)
  }
  override def jsToAny: PartialFunction[JsValue, Any] = {
    case o if (o \ "type").as[String] == "Option" =>
      if ((o \ "isDefined").as[Boolean]) { Some(jsToAny((o \ "val").get)) } else { None }
  }
}

trait JsonCollectionSerialization extends JsonAnySerialization {
  override def anyToJs: PartialFunction[Any, JsValue] = {
    case l: List[_] => Json.obj("type" -> "List", "list" -> JsArray(l.map(anyToJs)))
    case s: Set[_] => Json.obj("type" -> "Set", "set" -> JsArray(s.toList.map(anyToJs)))
    case m: Map[_, _] => Json.obj("type" -> "Map", "keys" -> anyToJs(m.keys.toList), "values" -> anyToJs(m.values.toList))
  }
  override def jsToAny: PartialFunction[JsValue, Any] = {
    case l if (l \ "type").as[String] == "List" => (l \ "list").as[JsArray].value.map(jsToAny).toList
    case s if (s \ "type").as[String] == "Set" => (s \ "set").as[JsArray].value.map(jsToAny).toSet
    case m if (m \ "type").as[String] == "Map" =>
      (jsToAny((m \ "keys").get).asInstanceOf[List[Any]] zip jsToAny((m \ "values").get).asInstanceOf[List[Any]]).toMap
  }
}

trait JsonTupleSerialization extends JsonAnySerialization {
  override def anyToJs: PartialFunction[Any, JsValue] = {
    case t if isTuple(t) => Json.obj("type" -> "Tuple", "values" -> anyToJs(t.asInstanceOf[Product].productIterator.toList))
  }
  override def jsToAny: PartialFunction[JsValue, Any] = {
    case t if (t \ "type").as[String] == "Tuple" => listToTuple(jsToAny((t \ "values").get).asInstanceOf[List[Any]])
  }

  private def isTuple(obj: Any) = obj.getClass.getName.contains("scala.Tuple")
  private def listToTuple(list: List[Any]): Product = {
    val tupleClass = Class.forName("scala.Tuple" + list.size)
    tupleClass.getConstructors.apply(0).newInstance(list.asInstanceOf[Seq[Object]]:_*).asInstanceOf[Product]
  }
}

trait JsonCommonFunctionSerialization extends JsonAnySerialization {
  override def anyToJs: PartialFunction[Any, JsValue] = {
    case f if isFunction(f) =>
      Json.obj("type" -> "Function", "name" -> anyToJs(f.getClass.getSimpleName.split("/")(0))) // TODO: verify if fragile
  }
  override def jsToAny: PartialFunction[JsValue, Any] = {
    case f if (f \ "type").as[String] == "Function" =>
      val fun = jsToAny((f \ "name").get).asInstanceOf[String]
      val fields = getClass.getDeclaredFields.map(fl => { fl.setAccessible(true); fl.get(this) }).filterNot(_ == null)
      fields.find(_.getClass.getSimpleName.split("/")(0) == fun).get
  }

  private def isFunction(obj: Any): Boolean = obj match {
    case _:Function0[_] | _:Function1[_,_] | _:Function2[_,_,_] | _:Function3[_,_,_,_] | _:Function4[_,_,_,_,_] |
              _:Function5[_,_,_,_,_,_] | _:Function6[_,_,_,_,_,_,_] | _:Function7[_,_,_,_,_,_,_,_] |
              _:Function8[_,_,_,_,_,_,_,_,_] | _:Function9[_,_,_,_,_,_,_,_,_,_] => true
    case _ => false
  }
}
