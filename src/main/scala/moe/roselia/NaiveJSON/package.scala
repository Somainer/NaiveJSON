package moe.roselia

package object NaiveJSON {
  def parseJSON: String => Either[List[(Int, String)], JSON] = JSON.parseJSON
  def parseJSON_!!(s: String): JSON = JSON.json >> s get_!!
}
