package moe.roselia.NaiveJSON

object States {
  case class Location(input: String, offset: Int = 0) {
    lazy val line: Int = input.slice(0, offset + 1).count('\n' == _) + 1
    lazy val col: Int = offset - input.slice(0, offset + 1).lastIndexOf('\n')

    def toError(msg: String): ParseError = ParseError(List((this, msg)))
  }

  case class ParseError(stack: List[(Location, String)]) {
    def push(location: Location, msg: String): ParseError =
      copy(stack = (location, msg)::stack)
    def latest: Option[(Location, String)] = stack.lastOption
    def latestLoc: Option[Location] = latest map (_._1)
    def label(msg: String): ParseError = ParseError(latestLoc map ((_, msg)) toList)
    def getTrace:String = stack.map{
      case (l, s) =>
        s"at: Line ${l.line}\n  in #${l.col} Found Error: $s\n"
    }.reduce(_+_)
    def printTrace_!(): Unit = {
      println(getTrace)
    }
  }

  type Parser[+A] = ParseState => Result[A]

  case class ParseState(loc: Location){
    def input: String = loc.input.substring(loc.offset)
    def slice(n: Int): String = loc.input.substring(loc.offset, loc.offset + n)
    def advanceBy(n: Int): ParseState = copy(loc = loc.copy(offset = loc.offset + n))
  }

  object ParseState{
    def fromString(s: String, loc: Int = 0) = ParseState(Location(s, loc))
  }

  trait Result[+A] {
    def mapError(f: ParseError => ParseError): Result[A] = this match {
      case Failure(e, c) => Failure(f(e), c)
      case _ => this
    }
    def mapSuccess[B >: A](f: A => B): Result[B] = this match {
      case Success(r, c) => Success(f(r), c)
      case _ => this
    }
    def unCommit: Result[A] = this match {
      case Failure(e, true) => Failure(e, false)
      case _ => this
    }
    def addCommit(isCommitted: Boolean): Result[A] = this match {
      case Failure(e, c) => Failure(e, c || isCommitted)
      case _ => this
    }
    def advanceSuccess(n: Int): Result[A] = this match {
      case Success(r, c) => Success(r, c + n)
      case _ => this
    }
    def getOption : Option[A] = this match {
      case Success(g, _) => Some(g)
      case _ => None
    }
    def get_!! : A = this match {
      case Success(r, _) => r
      case Failure(e, _) => throw new IllegalArgumentException(e.getTrace)
    }
    def getOrElse[B >: A](els: B): B = getOption getOrElse els
    def getEither: Either[List[(Int, String)], A] = this match {
      case Success(r, _) => Right(r)
      case Failure(e, _) => Left(e.stack.map {
        case (loc, s) => (loc.offset, s)
      })
    }
  }
  case class Success[+A](get: A, charsConsumed: Int) extends Result[A]
  case class Failure(get: ParseError, isCommitted: Boolean) extends Result[Nothing]
}
