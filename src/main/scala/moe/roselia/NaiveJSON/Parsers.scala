package moe.roselia.NaiveJSON

import java.util.regex.Pattern

import moe.roselia.NaiveJSON.Implicits._
import moe.roselia.NaiveJSON.States._

import scala.util.Try
import scala.util.matching.Regex

trait Parsers {
  self =>

  def run[A](p: Parser[A])(input: String): Either[ParseError, A] =
    p(ParseState(Location(input))) extract input

  def or[A](s1: Parser[A], s2: => Parser[A]): Parser[A] = s =>
    s1(s) match {
      case Failure(_, false) => s2(s)
      case r => r
    }

  def map[A, B](p: Parser[A])(f: A => B): Parser[B] =
    p flatMap (a => succeed(f(a)))

  def flatMap[A, B](a: Parser[A])(f: A => Parser[B]): Parser[B] = s =>
    a(s.unsliced) match {
      case Success(r, c) => f(r)(s.advanceBy(c)) addCommit (c != 0) advanceSuccess c
      case Slice(n) => f(s.slice(n).asInstanceOf[A])(s.advanceBy(n).reslice(s)) advanceSuccess n
      case e@Failure(_, _) => e
    }

  def product[A, B](p: Parser[A], p2: => Parser[B]): Parser[(A, B)] =
    map2(p, p2)((_, _))

  def map2[A, B, C](p: Parser[A], p2: => Parser[B])(f: (A, B) => C): Parser[C] = for {
    x <- p
    y <- p2
  } yield f(x, y)

  def succeed[A](a: A): Parser[A] = _ => Success(a, 0)

  def failure[A](error: ParseError, commit: Boolean = true): Parser[A] = _ => Failure(error, commit)

  def many[A](p: Parser[A]): Parser[List[A]] =
    map2(p, many(p))(_ :: _) | succeed(Nil)

  def listOfN[A](n: Int, p: Parser[A]): Parser[List[A]] =
    if (n > 0) map2(p, listOfN(n - 1, p))(_ :: _) else succeed(Nil)

  def label[A](msg: String)(p: Parser[A]): Parser[A] =
    s => p(s) mapError (_.label(msg))

  def scope[A](msg: String)(p: Parser[A]): Parser[A] =
    s => p(s) mapError (_.push(s.loc, msg))

  def attempt[A](p: Parser[A]): Parser[A] = s =>
    p(s).unCommit

  def string(s: String): Parser[String] = input => {
    val msg = s"Expect String: " + s.toQuoted
    if (input.input.startsWith(s)) Success(s, s.length)
    else Failure(input.loc.toError(msg), false)
  }

  def all: Parser[String] = input => Success(input.input, input.input.length)

  def transformBefore(f: String => String): Parser[String] = all map f

  def transform[A](p: Parser[A])(f: String => String): Parser[A] = input => p >> f(input.input)

  implicit def toStringParser(s: String): Parser[String] = string(s)

  def slice[A](p: Parser[A]): Parser[String] =
    s => p(s sliced).slice

  implicit def operators[A](p: Parser[A]): ParserOps[A] = ParserOps(p)

  implicit def regex(r: Regex): Parser[String] = s => r.findPrefixOf(s.input) match {
    case None => Failure(s.loc.toError(s"Match: /$r/"), false)
    case Some(m) => Success(m, m.length)
  }

  def as[A, B](p: Parser[A])(b: B): Parser[B] = slice(p token) map (_ => b)

  def skipL[A, B](p: Parser[A], sk: Parser[B]): Parser[B] = slice(p).map2(sk)((_, b) => b)

  def skipR[A, B](p: Parser[A], sk: Parser[B]): Parser[A] = p.map2(sk slice)((a, _) => a)

  def whiteSpace: Parser[String] = "\\s*".r

  def digit: Parser[String] = "^(0|[1-9][0-9]*|-[1-9][0-9]*)".r.token label "Int Literal (Non Double)"

  def token[A](p: Parser[A]): Parser[A] = whiteSpace *> attempt(p) <* whiteSpace

  def sepgt1[A, B](p: Parser[A], spt: Parser[B]): Parser[List[A]] = map2(p, many(spt *> whiteSpace *> p))(_ :: _)

  def sep[A, B](p: Parser[A], spt: Parser[B]): Parser[List[A]] =
    sepgt1(p, spt) | succeed(Nil)

  def surround[A, B, C](s: Parser[A], t: Parser[B])(p: Parser[C]): Parser[C] =
    s.token *> p <* t.token

  def eof: Parser[String] =
    regex("\\z".r) label "Unexpected tailing literal."

  def root[A](p: Parser[A]): Parser[A] =
    p <* eof

  def thru(s: String): Parser[String] =
    (".*?" + Pattern.quote(s)).r

  def quoted: Parser[String] =
    "\"" *> thru("\"").map(_ dropRight 1)

  def escapedQuoted: Parser[String] =
    token(quoted label "string literal")

  def doubleString: Parser[String] = token("[-+]?([0-9]*\\.)?[0-9]+([eE][-+]?[0-9]+)?".r)

  def strictDoubleString: Parser[String] = token("[-+]?([0-9]*\\.)[0-9]+([eE][-+]?[0-9]+)?".r)

  def double: Parser[Double] = doubleString map (_ toDouble) label "Double literal"

  def double_!! : Parser[Double] = ~(strictDoubleString map (_ toDouble) label "Double literal")

  def int_1: Parser[Int] = digit map (_ toInt) label "Integer literal"

  def int_!! : Parser[Int] = digit flatMap (s =>
    Try(s.toInt) toOption match {
      case Some(a) => succeed(a)
      case None => failure(ParseError(List((Location(s), s"Integer $s out of range"))))
    }) scope "Integer literal"

  def int = ~int_!!


  implicit def asStringParser[A](a: A)(implicit f: A => Parser[String]): ParserOps[String] = ParserOps(f(a))

  def char(c: Char): Parser[Char] = string(c.toString) map (_.charAt(0))

  case class ParserOps[A](p: Parser[A]) {
    def |[B >: A](p2: => Parser[B]): Parser[B] = self.or(p, p2)

    def or[B >: A](p2: => Parser[B]): Parser[B] = self.or(p, p2)

    def product[B](p2: Parser[B]): Parser[(A, B)] = self.product(p, p2)

    def **[B](p2: Parser[B]): Parser[(A, B)] = self.product(p, p2)

    def &[B](p2: Parser[B]): Parser[(A, B)] = self.product(p, p2)

    def map[B](f: A => B): Parser[B] = self.map(p)(f)

    def |>[B](f: A => B): Parser[B] = self.map(p)(f)

    def map2[B, C](b: Parser[B])(f: (A, B) => C): Parser[C] = self.map2(p, b)(f)

    def flatMap[B](f: A => Parser[B]): Parser[B] = self.flatMap(p)(f)

    def label(msg: String): Parser[A] = self.label(msg)(p)

    def scope(msg: String): Parser[A] = self.scope(msg)(p)

    def slice: Parser[String] = self.slice(p)

    def token: Parser[A] = self.token(p)

    def <=>[B](b: B): Parser[B] = self.as(p)(b)

    def ⇔[B](b: B): Parser[B] = self.as(p)(b)

    def unary_~ : Parser[A] = self.attempt(p)

    def *>[B](p2: => Parser[B]): Parser[B] = self.skipL(p, p2)

    def <*(p2: => Parser[Any]): Parser[A] = self.skipR(p, p2)

    def <|>[B](sep: Parser[B]): Parser[List[A]] = self.sep(p, sep)

    def test(s: String) = p(ParseState.fromString(s))

    def >>(s: String) = this test s

    def printTestTrace_! = test _ andThen (_.mapSuccess(s => {
      println("Success! got: " + s)
      s
    }).mapError(e => {
      e.printTrace_!()
      e
    }))

    def >>! = printTestTrace_!
  }

}
