package moe.roselia.NaiveJSON

import moe.roselia.NaiveJSON.JSONStruct._

import scala.languageFeature.{implicitConversions, postfixOps}
import scala.util.Try

trait Implicits {
  implicit def toNaiveString(s: String): NaiveString = NaiveString(s)

  def parse[T: ParseOp](s: String): Option[T] = Try {
    implicitly[ParseOp[T]].op(s)
  } toOption

  def <:>(js: Seq[JSON]) = JArray(js.toIndexedSeq)

  def <::>(js: JSON*) = JArray(js.toIndexedSeq)

  def JArrayOf(js: JSON*) = JArray(js.toIndexedSeq)

  implicit val popDouble = ParseOp[Double](_.toDouble)
  implicit val popInt = ParseOp[Int](_.toInt)
  implicit val popLong = ParseOp[Long](_.toLong)
  implicit val popFloat = ParseOp[Float](_.toFloat)
  implicit val popString = ParseOp[String](_.toString)

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

  implicit def toObject(implicit ts: Map[String, JSON]): JObject = JObject(ts)

  //implicit def toObject(implicit ts: Seq[(JString, JSON)]):JObject = JObject(ts.toMap)
  implicit def toObject(implicit ts: Seq[(String, JSON)]): JObject = JObject(ts.toMap)

  //implicit def toObject(ts: (String, JSON)):JObject = toObject(Map(ts))
  implicit def toObject(ts: (JString, JSON)): JObject = toObject(Map(ts._1.get -> ts._2))

  //def <--> (implicit js: (JString, JSON)*) = JObject(js.map(strUnTup).toMap)
  def <++>(js: JObject*): JObject = JObjectOf(js: _*)

  def JObjectOf(js: JObject*): JObject = if (js.isEmpty) JObject(Map()) else JObject(js.map(_.get).reduce(_ ++ _))

  def <->(js: Seq[(JString, JSON)]) = JObject(js.map(strUnTup).toMap)

  case class NaiveString(s: String) {
    def indentOf(indent: Int)(dep: Int = 0): String = " " * (dep * indent) + s

    def toEscapedQuoted: String = toEscaped toQuoted

    def toQuoted: String = "\"" + s + "\""

    def toEscaped: String = NaiveString.convertStr.foldRight(s) {
      case ((frm, to), acc) => acc.replace(frm, to)
    }

  }

  case class ParseOp[T](op: String => T)

  object NaiveString {
    lazy val convertStr = List(
      ("'", "\\'"),
      ("\n", "\\\\n"),
      ("\t", "\\\\t"),
      ("\r", "\\\\r"),
      ("\"", "\\\""),
      ("\\", "\\\\")
    )

    def fromEscaped(s: String): String = convertStr.foldRight(s) {
      case ((to, frm), acc) => acc.replace(frm, to)
    }
  }

  //def <-> (js: Seq[JObject]) = JObjectOf(js:_*)
}

object Implicits extends Implicits with MaybeJSON