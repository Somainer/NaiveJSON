package moe.roselia.NaiveJSON.Dynamic

import moe.roselia.NaiveJSON.JSONStruct._
import moe.roselia.NaiveJSON._


object DynamicJSON extends MaybeJSON {

  case class DynamicJSON(get: Option[JSON]) extends MaybeJSON(get) with Dynamic {
    import scala.language.dynamics
    def !! :JSON = get.get
    def toDynamic: DynamicJSON = this

    def selectDynamic(field: String): DynamicJSON = DynamicJSON(get.subVal(field))

    def toOption: Option[JSON] = get

  }

  implicit def toJSONDynamic(jSON: JSON): DynamicJSON = DynamicJSON(Some(jSON))

  implicit def toJSONDynamic(jSON: Option[JSON]): DynamicJSON = DynamicJSON(jSON)

  implicit def fromJSONDynamic(jSON: DynamicJSON): Option[JSON] = jSON.get
}
