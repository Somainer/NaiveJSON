package moe.roselia.NaiveJSON
import Implicits._

trait JSONStruct {
  def strUnTup(t: (JString, JSON)): (String, JSON) = t._1.get -> t._2
  case object JNull extends JSON {
    override def literal = "null"
  }
  case class JDouble(get: Double) extends JSON {
    override def literal: String = get.toString
  }
  case class JInt(get: Int) extends JSON {
    override def literal: String = get.toString
  }
  case class JString(get: String) extends JSON {
    override def literal: String = get.toQuoted
    def :->(j: JSON): (JString, JSON) = {
      (this, j)
    }
    def :->(n: Null): (JString, JSON) = this :-> JNull
    def :->(js: (JString, JSON)*): JObject = {
      JObject(Map(this.get -> JObject(js.map(t => (t._1.get, t._2)).toMap)))
    }
    def :-(implicit j:JSON) = this :-> j
    def :-(implicit js: (JString, JSON)*):JObject = this :-> (js:_*)
  }
  case class JBool(get: Boolean) extends JSON {
    override def literal: String = get.toString
  }
  case class JArray(get: IndexedSeq[JSON]) extends JSON {
    override def toStr(dep: Int = 0): String = get.map(_.toString.depOf(dep + 1)).mkString("[", ", ", "]")
    override def literal: String = toStr()
    def !!(idx: Int) = get(idx)
  }
  case class JObject(get: Map[String, JSON]) extends JSON {
    override def toStr(dep: Int = 0): String = (for{
      (k, v) <- get
    } yield k.toQuoted + s": ${v.toString depOf (dep + 1)}").mkString("{", ", ", "}")

    override def literal: String = toString

    def map(f: Map[String, JSON] => Map[String, JSON]):JObject =
      JObject(f(get))

    def <+>(implicit k: String, v: JSON): JObject = map(_ + (k -> v))
    def <+>(implicit j: JObject): JObject = map(_ ++ j.get)
    //def +(k: JString, v: JSON): JObject = JObject(get + (k.get -> v))
  }

}

object JSONStruct extends JSONStruct
