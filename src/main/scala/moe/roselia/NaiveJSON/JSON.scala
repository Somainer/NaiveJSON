package moe.roselia.NaiveJSON

import java.util.regex.Pattern

import moe.roselia.NaiveJSON.Implicits._
import moe.roselia.NaiveJSON.JSONStruct._
import moe.roselia.NaiveJSON.States._

import scala.util.Try

trait JSON {
  def literal: String

  override def toString: String = literal

  def as_!![T]: T = asInstanceOf[T]

  def as[T]: Option[T] = Try(as_!![T]).toOption

  def toJSON: JSON = this

  def unary_~ : JSON = this

  def getVal: Any

  def getAs[T: ParseOp]: Option[T] = getVal match {
    case x: String => parse[T](x)
    case x => Try(x.asInstanceOf[T]).toOption
  }

  def getAs_!![T: ParseOp]: T = getAs[T].get

  def subVal(key: Int): Option[JSON] = None

  def subVal_!!(key: Int): JSON = subVal(key) get

  def subVal(key: String): Option[JSON] = None

  def subVal_!!(key: String): JSON = subVal(key) get

  def format(indent: Int = 2, dep: Int = 0): String = this match {
    case JObject(g) => "{\n" +
      g.map {
        case (k, v) => k.toEscapedQuoted.indentOf(indent)(dep + 1) + s": ${v.format(indent, dep + 1)}"
      }.mkString(",\n") +
      "\n" + "}".indentOf(indent)(dep)
    case JArray(g) => "[\n" +
      g.map(_.format(indent, dep + 1).indentOf(indent)(dep + 1)).mkString(",\n") +
      "\n" + "]".indentOf(indent)(dep)
    case json => json.literal
  }

  def format: String = format()

  def typeName: String = this match {
    case JNull => "Null"
    case JArray(_) => "Array"
    case JObject(_) => "Object"
    case JString(_) => "String"
    case JBool(_) => "Boolean"
    case JInt(_) => "Number"
    case JDouble(_) => "Number"
    case _ => "Undefined"
  }
}


object JSON {
  val struct = JSONStruct
  def fromValue(v: Any):JSON = v match {
    case x:Int => JInt(x)
    case x:Double => JDouble(x)
    case x:Boolean => JBool(x)
    case x:String => JString(x)
    case x:Seq[Any] => JArray(x.map(fromValue).toIndexedSeq)
    case x:Map[String, Any] => JObject(x.map {
      case (k, va) => k -> fromValue(va)
    })
    case _ => JNull
  }
  def fromValueOption(v: Any):Option[JSON] = v match {
    case null => Some(JNull)
    case x => fromValue(x) match {
      case JNull => None
      case els => Some(els)
    }
  }
}