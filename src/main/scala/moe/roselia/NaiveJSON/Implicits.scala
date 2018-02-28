package moe.roselia.NaiveJSON
import JSONStruct._

trait Implicits {
  implicit class NaiveString(s: String){
    def toQuoted: String = "\"" + s + "\""
    def depOf(dep: Int = 0): String = "  " * (dep*0) + s
  }

  implicit def toJString(s: String): JString = JString(s)
  implicit def fromJString(s: JString): String = s.get
  implicit def fromJArray(arr: JArray): IndexedSeq[JSON] = arr.get
  implicit def toJArray(arr: Seq[JSON]): JArray = JArray(arr.toIndexedSeq)
  implicit def toJInt(i: Int): JInt = JInt(i)
  implicit def toNull(x: Null) = JNull
  implicit def toBool(b: Boolean) = JBool(b)
  implicit def toDouble(d: Double) = JDouble(d)
  implicit def toObject(implicit ts: Map[String, JSON]):JObject = JObject(ts)
  //implicit def toObject(implicit ts: Seq[(JString, JSON)]):JObject = JObject(ts.toMap)
  implicit def toObject(implicit ts: Seq[(String, JSON)]):JObject = JObject(ts.toMap)
  //implicit def toObject(ts: (String, JSON)):JObject = toObject(Map(ts))
  implicit def toObject(ts: (JString, JSON)):JObject = toObject(Map(ts._1.get -> ts._2))


  def /: (js: JSON*) = JArray(js.toIndexedSeq)
  def /> (js: (JString, JSON)*) = JObject(js.map(strUnTup).toMap)
}
object Implicits extends Implicits