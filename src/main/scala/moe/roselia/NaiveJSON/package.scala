package moe.roselia

import moe.roselia.NaiveJSON.JSONParser._

package object NaiveJSON {
  lazy val parser = JSONParser
  lazy val dynamicJSON = Dynamic.DynamicJSON
  lazy val DSL = NaiveDSL
  lazy val reflect = Reflect.NaiveReflect

  def parseJSON: String => Either[List[(Int, String)], JSON] = parser.parseJSON

  def parseJSON_!!(s: String): JSON = parser.json >> s get_!!
}
