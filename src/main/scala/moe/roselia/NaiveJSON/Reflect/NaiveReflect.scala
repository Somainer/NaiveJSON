package moe.roselia.NaiveJSON.Reflect

import moe.roselia.NaiveJSON.JSON
import moe.roselia.NaiveJSON.JSONStruct._

import scala.reflect._
import scala.reflect.runtime.universe._

object NaiveReflect {
  /**
    * fromPlainClass[T](obj, objMapper)
    *
    * @param obj       :T An object of T, convert to JSON according to objMapper
    *                  T MUST BE a top-leveled class, not nested class.
    * @param objMapper : String => Option[String] = Some(_)
    *                  A function, passing keys and returns Option[String] objMapper(key) match {
    *                     case Some(newKey) => Replace (key, value) to (newKey, value) in obj
    *                     case None => This field will not included.
    *                  }
    * @return JSON
    **/
  def fromPlainClass[A: TypeTag : ClassTag](obj: A, objMapper: String => Option[String] = Some(_)): JSON = {
    JSON.fromValueOption(obj) getOrElse JObject {
      val cls = obj.getClass
      val flds = cls.getDeclaredFields
      val mirror = runtimeMirror(cls.getClassLoader)
      val classObj = mirror.reflect(obj)
      val typTag = classObj.symbol.toType
      flds.flatMap { typ =>
        val name = typ.getName
        objMapper(name) map { fldName =>
          val decl = typTag.decl(TermName(name))
          val term = decl.asTerm
          val symbol = classObj.reflectField(term)
          val res = symbol.get
          fldName -> fromPlainClass(res)
        }
      }.toMap
    }
  }

  def toPlainClass[T: TypeTag : ClassTag](obj: JSON): T = {
    ???
    // TODO: Convert JSON to a class
  }


}
