package moe.roselia.NaiveJSON.Macros

import scala.language.experimental.macros
import scala.reflect.macros.whitebox._
import moe.roselia.NaiveJSON.JSON

import scala.reflect.macros.whitebox


trait JSONProtocol {
  type Mapper = String => Option[String]
  def toJSON: Any = macro JSONProtocol.jsonIMPL

}
object JSONProtocol {
  type Mapper = String => Option[String]
  def jsonIMPL(c: whitebox.Context) = {
    import c.universe._
    val fields = this.getClass.getDeclaredFields
    ???
    // TODO: Compile-time JSON serialize
  }
}
