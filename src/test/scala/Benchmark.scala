import moe.roselia.NaiveJSON.NaiveDSL._
import moe.roselia.NaiveJSON.JSON
import moe.roselia.NaiveJSON

import java.util.Date

object Benchmark {
  def makeJSON(cnt: Int): JSON = JObjectOf("people" :- <:>((1 to cnt).map(i => {
    JObjectOf("id" :- i, "number" :- i.toString)
  })), "total" :- cnt)

  def testToString(cnt: Int): Unit = {
    inspect("Building JSON of", cnt, "elements.")
    val pst = System.currentTimeMillis()
    val js = makeJSON(cnt)
    val ped = System.currentTimeMillis()
    inspect("Finish in", ped - pst, "ms")

    inspect("Formating JSON of", cnt, "elements.")
    val st = System.currentTimeMillis()
    val str = js.format
    val ed = System.currentTimeMillis()
    inspect("Finish in", ed - st, "ms")
    ///*
    inspect("Parsing JSON of", cnt, "elements.")
    val pst2 = System.currentTimeMillis()
    val js2 = NaiveJSON.parseJSON_!!(str)
    val ped2 = System.currentTimeMillis()
    inspect("Finish in", ped2 - pst2, "ms")
    inspect("Should be true", js2 == js) //*/
  }

  def inspect(any: Any*): Unit = {
    println(any.mkString(" "))
  }

  def main(args: Array[String]): Unit = {
    1 to 6 map ("1" + "0" * _) map (_.toInt) foreach testToString
  }
}
