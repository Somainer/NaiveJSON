package moe.roselia.NaiveJSON
import moe.roselia.NaiveJSON.States._
import moe.roselia.NaiveJSON.Implicits._
import JSONStruct._

trait JSON {
  def toStr(dep: Int = 0): String = literal depOf dep
  def literal: String
  override def toString: String = toStr()
  def toJSON: JSON = this
  def unary_~ :JSON = this
}

object JSON extends Parsers {

  def jsonParser: Parser[JSON] = {
    //import P._
    def nil = "null" <=> JNull
    def bool = ("true" <=> true) |
      ("false" <=> false)
    def jbool = bool map JBool
    def literal = scope("Literal"){
      nil | jbool | escapedQuoted.map(JString) | double_!!.map(JDouble) | int.map(JInt) | double.map(JDouble)
    }
    def values = literal | array | jObject
    def keyVal = escapedQuoted & (":".token *> values)
    def array: Parser[JSON] = surround("[", "]")(
      values <|> "," map (s => JArray(s.toIndexedSeq))) scope "Array"

    def jObject: Parser[JSON] = surround("{", "}")(
      keyVal <|> "," map (s => JObject(s.toMap))) scope "Object"

    //root(whiteSpace *> values)
    whiteSpace *> values <* eof
  }
  //def naiveJSONParser: Parser[JSON] = jsonParser(this)
  def json(s: String): Result[JSON] = jsonParser test s
}