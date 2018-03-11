package moe.roselia.NaiveJSON

import moe.roselia.NaiveJSON.Implicits._

import scala.util.Try

trait JSONStruct {
  def strUnTup(t: (JString, JSON)): (String, JSON) = t._1.get -> t._2

  case object JNull extends JSON {
    override def literal = "null"

    override def getVal: Null = null
  }

  case class JDouble(get: Double) extends JSON {
    override def literal: String = get.toString

    override def getVal: Double = get
  }

  case class JInt(get: Int) extends JSON {
    override def literal: String = get.toString

    override def getVal: Int = get
  }

  case class JString(get: String) extends JSON {
    override def getVal: String = get

    override def literal: String = get.toEscapedQuoted

    def :->(j: JSON): (JString, JSON) = {
      (this, j)
    }

    def :->(n: Null): (JString, JSON) = this :-> JNull

    def :->(js: (JString, JSON)*): JObject = {
      JObject(Map(this.get -> JObject(js.map(t => (t._1.get, t._2)).toMap)))
    }

    def :-(j: JSON) = this :-> j

    def :-(js: JObject*): JObject = JObject(Map(get -> JObject(js.map(_.get).reduce(_ ++ _))))

    //def to(js: JObject*):JObject = this :- (js:_*)
    def to(js: JSON) = this :- js
  }

  case class JBool(get: Boolean) extends JSON {
    override def getVal: Boolean = get

    override def literal: String = get.toString
  }

  case class JArray(get: IndexedSeq[JSON]) extends JSON {
    def this(js: JSON*) {
      this(js.toIndexedSeq)
    }

    override def getVal: IndexedSeq[JSON] = get

    override def literal: String = get.map(_.toString).mkString("[", ", ", "]")

    def !!(idx: Int) = get(idx)

    override def subVal(key: Int): Option[JSON] = Try(get(key)).toOption

    override def subVal_!!(key: Int): JSON = subVal(key) head

    def map(f: JSON => JSON): JArray = JArray(get.map(f))

    def apply(key: Int): Option[JSON] = subVal(key)

    def withFilter = get.withFilter _
  }

  case class JObject(get: Map[String, JSON]) extends JSON {
    def this(js: JObject*) {
      this(js.map(_.get).reduce(_ ++ _))
    }

    override def getVal: Map[String, JSON] = get

    def withFilter = get.withFilter _

    override def literal: String = get.map {
      case (k, v) => k.toQuoted + s": ${v.toString}"
    }.mkString("{", ", ", "}")

    def map(f: Map[String, JSON] => Map[String, JSON]): JObject =
      JObject(f(get))

    override def subVal(key: String): Option[JSON] = get.get(key)

    override def subVal_!!(key: String): JSON = subVal(key) head

    def apply(key: String): Option[JSON] = subVal(key)

    def <+>(implicit k: String, v: JSON): JObject = map(_ + (k -> v))

    def <+>(implicit j: JObject): JObject = map(_ ++ j.get)

    //def +(k: JString, v: JSON): JObject = JObject(get + (k.get -> v))
  }

}

object JSONStruct extends JSONStruct
