import moe.roselia.NaiveJSON.NaiveDSL._
import moe.roselia.NaiveJSON.JSON._
object RoseliaBlog {
  def testAST(): Unit ={
    val sample = ("main" :-> (
      "number" :- 123,
      "double" :- 233,
      "shouldBeDouble" :- 1234123423451234342134D,
      "bool" :- true,
      "null" :- JNull
    )) <+> "d" :- 1
    println(sample)
    jsonParser >>! sample.toString
  }

  def main(args: Array[String]): Unit = {
    testAST()
  }
}
