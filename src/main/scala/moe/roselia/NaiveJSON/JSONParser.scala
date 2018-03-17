package moe.roselia.NaiveJSON

import java.util.regex.Pattern

import moe.roselia.NaiveJSON.Implicits._
import moe.roselia.NaiveJSON.JSONStruct._
import moe.roselia.NaiveJSON.States._

object JSONParser extends Parsers {
  self =>

  implicit class JsonHelper(val sc: StringContext) extends AnyVal {
    def naiveJSON_!!(args: Any*): JSON = self.json >> sc.s(args: _*) get_!!

    def naiveJSON(args: Any*): Option[JSON] = self.json >> sc.s(args: _*) getOption
  }

  def findFirstNonMatch(s: String, s2: String, offset: Int): Int = {
    @annotation.tailrec
    def _inner(p1: Int, p2: Int): Int =
      if (p1 < s.length && p2 < s2.length) {
        if (s.charAt(p1) != s2.charAt(p2)) p2
        else _inner(p1 + 1, p2 + 1)
      } else {
        if (s.length >= s2.length + offset) -1
        else s.length - offset
      }

    _inner(offset, 0)
  }

  override def string(w: String): Parser[String] = s => {
    val idx = findFirstNonMatch(s.loc.input, w, s.loc.offset)
    if (idx < 0) {
      if (s.isSliced) Slice(w.length) else Success(w, w.length)
    } else Failure(s.loc.advanceBy(idx).toError(s"Expect: ${w.toQuoted}"), idx != 0)
  }

  //A solution for StackOverFlow Exception
  override def many[A](p: Parser[A]): Parser[List[A]] = s => {
    if (s.isSliced) {
      def inner(p: Parser[String], offset: Int = 0): Result[String] = {
        p(s.advanceBy(offset)) match {
          case Slice(n) => inner(p, n + offset)
          case Failure(_, false) => Slice(offset)
          case f => f
        }
      }

      inner(p.slice).asInstanceOf[Result[List[A]]]
    } else {
      val buf = new collection.mutable.ListBuffer[A]

      def inner(p: Parser[A], offset: Int = 0): Result[List[A]] = {
        p(s.advanceBy(offset)) match {
          case Success(r, c) => buf += r; inner(p, offset + c)
          case Slice(n) =>
            buf += s.input.substring(offset, offset + n).asInstanceOf[A]
            inner(p, offset + n)
          case Failure(_, false) => Success(buf.toList, offset)
          case f@Failure(_, true) => f
        }
      }

      inner(p)
    }
  }

  override def thru(s: String): Parser[String] =
    (".*?[^\\\\](\\\\\\\\)*" + Pattern.quote(s)).r

  def nil = "null" <=> JNull label "JNull literal"

  def bool: Parser[Boolean] = ("true" <=> true) |
    ("false" <=> false)

  def jbool: Parser[JBool] = bool map JBool label "JBool literal"

  def literal = scope("JSON Literal") {
    nil | jbool | escapedQuoted.map(JString) | double_!!.map(JDouble) | int.map(JInt) | double.map(JDouble)
  }

  def values: Parser[JSON] = literal | array | jObject scope "JSON Values"

  def keyVal: Parser[(String, JSON)] = escapedQuoted & (":".token *> values)

  def array: Parser[JSON] = surround("[", "]")(
    values <|> "," map (s => JArray(s.toIndexedSeq))) scope "Array"

  def jObject: Parser[JSON] = surround("{", "}")(
    keyVal <|> "," map (s => JObject(s.toMap))) scope "Object"

  def json: Parser[JSON] = {
    root(whiteSpace *> values)
  }

  //def naiveJSONParser: Parser[JSON] = jsonParser(this)
  def parseJSON(s: String): Either[List[(Int, String)], JSON] = json test s getEither
}
