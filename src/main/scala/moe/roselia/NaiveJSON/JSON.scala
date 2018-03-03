package moe.roselia.NaiveJSON
import moe.roselia.NaiveJSON.States._
import moe.roselia.NaiveJSON.Implicits._
import JSONStruct._
import moe.roselia.NaiveJSON

import scala.util.Try

trait JSON {
  def literal: String
  override def toString: String = literal
  def as_!![T]: T = asInstanceOf[T]
  def as[T]: Option[T] = Try(as_!![T]) toOption
  def toJSON: JSON = this
  def unary_~ :JSON = this
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
      g.map{
      case (k, v) => k.toQuoted.indentOf(indent)(dep+1) + s": ${v.format(indent, dep+1)}"
    }.mkString(",\n") +
      "\n"+"}".indentOf(indent)(dep)
    case JArray(g) => "[\n" +
      g.map(_.format(indent, dep+1).indentOf(indent)(dep+1)).mkString(",\n") +
      "\n"+"]".indentOf(indent)(dep)
    case json => json.literal
  }
  def format: String = format()
}



object JSON extends Parsers { self =>
  implicit class JsonHelper(val sc:StringContext) extends AnyVal{
    def naiveJSON_!!(args:Any*):JSON = self.json >> sc.s(args:_*) get_!!
    def naiveJSON(args:Any*):Option[JSON] = self.json >> sc.s(args:_*) getOption
  }
  def nil = "null" <=> JNull label "JNull literal"
  def bool: Parser[Boolean] = ("true" <=> true) |
    ("false" <=> false)
  def jbool: Parser[JBool] = bool map JBool label "JBool literal"
  def literal = scope("JSON Literal"){
    nil | jbool | escapedQuoted.map(JString) | double_!!.map(JDouble) | int.map(JInt) | double.map(JDouble)
  }
  def values: Parser[JSON] = literal | array | jObject scope "JSON Values"
  def keyVal: Parser[(String, JSON)] = escapedQuoted & (":".token *> values)
  def array: Parser[JSON] = surround("[", "]")(
    values <|> "," map (s => JArray(s.toIndexedSeq))) scope "Array"

  def jObject: Parser[JSON] = surround("{", "}")(
    keyVal <|> "," map (s => JObject(s.toMap))) scope "Object"

  def json: Parser[JSON] = {
    //import P._


    //root(whiteSpace *> values)
    whiteSpace *> values <* eof
  }
  //def naiveJSONParser: Parser[JSON] = jsonParser(this)
  def parseJSON(s: String): Either[List[(Int, String)], JSON] = json test s getEither
}