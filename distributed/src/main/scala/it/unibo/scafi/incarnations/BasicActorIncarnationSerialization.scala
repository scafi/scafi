/*
 * Copyright (C) 2016-2017, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENCE.txt file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package it.unibo.scafi.incarnations

import it.unibo.scafi.distrib.actor.{BaseCustomSerializer, CustomSerializer}
import play.api.libs.json._
import scala.collection.mutable.{Map => MMap}

trait BasicActorIncarnationSerializer extends BaseCustomSerializer { self: BasicAbstractActorIncarnation =>
  CustomSerializer.incarnation = Some(this)

  override def uidToJs(uid: UID): JsValue = Json.obj("id" -> uid)
  override def jsToUid(js: JsValue): UID = (js \ "id").as[Int]

  /*override implicit val computationExportWrites: Writes[ComputationExport] = export =>
    export.root().getClass.getName match {
      case "java.lang.Integer" => Json.obj("root" -> JsNumber(export.root().asInstanceOf[Int]))
      case s => Json.obj("root" -> s.toString)
    }
  override implicit val computationExportReads: Reads[ComputationExport] = js => {
    val factory = new EngineFactory(); val export = factory.emptyExport()
    (js \ "root").get match {
      case n: JsNumber => export.put(factory.emptyPath(), n.as[Int])
      case s => export.put(factory.emptyPath(), s.toString)
    }
    JsSuccess { export }
  }*/

  override implicit val computationExportWrites: Writes[ComputationExport] = export =>
    Json.obj("map" -> export.getMap.asInstanceOf[Map[Path,Any]])

  override implicit val computationExportReads: Reads[ComputationExport] = js => {
    val export = new EngineFactory().emptyExport()
    val field = classOf[ExportImpl].getDeclaredField("map"); field.setAccessible(true)
    field.set(export, MMap((js \ "map").as[Map[Path,Any]].toSeq:_*)); field.setAccessible(false)
    JsSuccess { export }
  }

  /*implicit val expMapWrites: Writes[Map[Path, Any]] = map => Json.obj(map.map {
    case (k, v) => v match {
      case i:Int => val ret = (Json.toJson(k).as[String], Json.toJsFieldJsValueWrapper(i)); ret
      case _ => val ret = Json.toJson(k).as[String] -> Json.toJsFieldJsValueWrapper(v.toString); ret
    }
  }.toSeq: _*)
  implicit val expMapReads: Reads[Map[Path, Any]] = js => JsSuccess {
    js.as[JsObject].value.map {
      case (k, v) => v match {
        case n:JsNumber => (Json.toJson(k).as[Path], n.as[Int])
      }
    }.toMap
  }*/

  implicit val expMapWrites: Writes[Map[Path, Any]] = map =>
    Json.obj("keys" -> map.keys, "values" -> anyListToJs(map.values.toList))

  implicit val expMapReads: Reads[Map[Path, Any]] = js =>
    JsSuccess { ((js \ "keys").as[List[Path]] zip jsToAnyList((js \ "values").get)).toMap }

  /*implicit val anyWrites: Writes[Any] = {
    case i:Int => Json.obj("type" -> "Int", "val" -> i)
    case s:String => Json.obj("type" -> "String", "val" -> s)
  }
  implicit val anyReads: Reads[Any] = js => {
    (js \ "type").as[String] match {
      case "Int" => JsSuccess { (js \ "val").as[Int] }
      case "String" => JsSuccess { (js \ "val").as[String] }
    }
  }*/

  def anyListToJs(list: List[Any]): JsValue = JsArray(list.map(anyToJs))
  def jsToAnyList(js: JsValue): List[Any] = List(js.as[JsArray].value.map(jsToAny))

  def anyToJs(any: Any): JsValue = any match {
    case i:Int => Json.obj("type" -> "Int", "val" -> i)
    case s:String => Json.obj("type" -> "String", "val" -> s)
  }
  def jsToAny(js: JsValue): Any = (js \ "type").as[String] match {
    case "Int" => (js \ "val").as[Int]
    case "String" => (js \ "val").as[String]
  }

  implicit val pathWrites: Writes[Path] = path => Json.obj("list" -> Json.toJson(path.asInstanceOf[PathImpl].path))
  implicit val pathReads: Reads[Path] = js => JsSuccess { new PathImpl((js \ "list").as[List[Slot]]) }

  implicit val slotWrites: Writes[Slot] = {
    case Nbr(index) => Json.obj("type"->"Nbr", "index"->index)
    case Rep(index) => Json.obj("type"->"Rep", "index"->index)
    case FoldHood(index) => Json.obj("type"->"FoldHood", "index"->index)
    case FunCall(index, funId) => funId match {
      case i:Int => Json.obj("type"->"FunCall", "index"->index, "funId"->i)
      case s => Json.obj("type"->"FunCall", "index"->index, "funId"->s.toString)
    }
    case Scope(key) => key match {
      case i:Int => Json.obj("type"->"Scope", "key"->i)
      case s => Json.obj("type"->"Scope", "key"->s.toString)
    }
  }
  implicit val slotReads: Reads[Slot] = js =>
    (js \ "type").as[String] match {
      case "Nbr" => JsSuccess { Nbr((js \ "index").as[Int]) }
      case "Rep" => JsSuccess { Rep((js \ "index").as[Int]) }
      case "FoldHood" => JsSuccess { FoldHood((js \ "index").as[Int]) }
      case "FunCall" => JsSuccess {
        js \ "funId" match {
          case n: JsNumber => FunCall((js \ "index").as[Int], n.as[Int])
          case s => FunCall((js \ "index").as[Int], s.as[String])
        }
      }
      case "Scope" => JsSuccess {
        js \ "key" match {
          case n: JsNumber => Scope(n.as[Int])
          case s => Scope(s.as[String])
        }
      }
    }
}
