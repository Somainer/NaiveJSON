package moe.roselia.NaiveJSON.Macros

import scala.language.experimental.macros
import scala.reflect.macros.whitebox.Context
import moe.roselia.NaiveJSON.JSONStruct._
import moe.roselia.NaiveJSON.JSON

object ClassLoader {//, objMapper: c.Expr[String => Option[String]]
  def fromPlainClassIMPL[T: c.WeakTypeTag](c: Context)(obj: c.Expr[T]): c.universe.Tree = {
    import c.universe._
    val cls = obj.actualType
    val Literal(Constant(o)) = obj.tree
    println(cls.decls)
    q"println($cls)"
  }
  //, objMapper: String => Option[String] = Some(_)
  //def fromPlainClass[T](obj: T) = macro fromPlainClassIMPL


}
