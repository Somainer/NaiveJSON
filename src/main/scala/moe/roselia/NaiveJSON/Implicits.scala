package moe.roselia.NaiveJSON
import JSONStruct._
import moe.roselia.NaiveJSON

import scala.util.Try
trait Implicits {
  implicit class NaiveString(s: String){
    def toQuoted: String = "\"" + s + "\""
    def depOf(dep: Int = 0): String = "  " * (dep*0) + s
  }

  def parse[T: ParseOp](s: String): Option[T] = Try { implicitly[ParseOp[T]].op(s) }  toOption
  case class ParseOp[T](op: String => T)
  implicit val popDouble = ParseOp[Double](_.toDouble)
  implicit val popInt = ParseOp[Int](_.toInt)
  implicit val popLong = ParseOp[Long](_.toLong)
  implicit val popFloat = ParseOp[Float](_.toFloat)

  implicit def toJString(s: String): JString = JString(s)
  implicit def fromJString(s: JString): String = s.get
  implicit def toJInt(i: Int): JInt = JInt(i)
  implicit def toNull(x: Null) = JNull
  implicit def toBool(b: Boolean) = JBool(b)
  implicit def toDouble(d: Double) = JDouble(d)
  implicit def fromJArray(arr: JArray): IndexedSeq[JSON] = arr.get
  implicit def toJArray(implicit arr: Seq[JSON]): JArray = JArray(arr.toIndexedSeq)
  implicit def toIntJArray(implicit arr: Seq[Int]): JArray = JArray(arr.toIndexedSeq.map(_ toJSON))
  implicit def toStringJArray(implicit arr: Seq[String]): JArray = JArray(arr.toIndexedSeq.map(_ toJSON))
  implicit def toDoubleJArray(implicit arr: Seq[Double]): JArray = JArray(arr.toIndexedSeq.map(_ toJSON))
  implicit def toBooleanJArray(implicit arr: Seq[Boolean]): JArray = JArray(arr.toIndexedSeq.map(_ toJSON))
  implicit def toObject(implicit ts: Map[String, JSON]):JObject = JObject(ts)
  //implicit def toObject(implicit ts: Seq[(JString, JSON)]):JObject = JObject(ts.toMap)
  implicit def toObject(implicit ts: Seq[(String, JSON)]):JObject = JObject(ts.toMap)
  //implicit def toObject(ts: (String, JSON)):JObject = toObject(Map(ts))
  implicit def toObject(ts: (JString, JSON)):JObject = toObject(Map(ts._1.get -> ts._2))

  def <:>(js: Seq[JSON]) = JArray(js.toIndexedSeq)
  def <::> (js: JSON*) = JArray(js.toIndexedSeq)
  def JArrayOf (js: JSON*) = JArray(js.toIndexedSeq)
  //def <--> (implicit js: (JString, JSON)*) = JObject(js.map(strUnTup).toMap)
  def <++> (js: JObject*) = JObjectOf(js:_*)
  def JObjectOf (js: JObject*): JObject = if(js.isEmpty) JObject(Map()) else JObject(js.map(_.get).reduce(_++_))
  def <-> (js: Seq[(JString, JSON)]) = JObject(js.map(strUnTup).toMap)
  //def <-> (js: Seq[JObject]) = JObjectOf(js:_*)
}
object Implicits extends Implicits