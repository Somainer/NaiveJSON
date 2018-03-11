package moe.roselia.NaiveJSON

trait MaybeJSON{

  implicit class MaybeJSON(get: Option[JSON]) {
    def map[A](f: JSON => A): Option[A] = get map f

    def flatMap[A](f: JSON => Option[A]): Option[A] = get flatMap f

    def getAs[B: Implicits.ParseOp]: Option[B] = flatMap (_.getAs[B])

    def as[A]: Option[A] = flatMap(_.as[A])

    def subVal(k: Int): Option[JSON] = flatMap(_.subVal(k))

    def subVal(k: String): Option[JSON] = flatMap(_.subVal(k))

    def apply(k: Int): Option[JSON] = subVal(k)

    def apply(k: String): Option[JSON] = subVal(k)

    def getOrElse[B >: JSON](els: => B):B = get getOrElse els

    def getVal: Option[Any] = map(_.getVal)

    def getVal_!! :Any = getVal.get

    def get_!! : JSON = get.get
  }
}

