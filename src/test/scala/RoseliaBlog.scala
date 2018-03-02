import moe.roselia.NaiveJSON.NaiveDSL._
import moe.roselia.NaiveJSON.JSON._
import moe.roselia.NaiveJSON
object RoseliaBlog {
  def testAST(): Unit ={
    val sample = JObjectOf ("main" :- (
      "number" :- 123,
      "double" :- 233.0,
      "shouldBeDouble" :- 1234123423451234342134D,
      "bool" :- true,
      "null" :- JNull,
      "ns":- "233",
      "obj" :- (
        "null" :- JNull,
        "a" :- "a",
        "arr" :- JArrayOf(JNull, false),
        "emptyObj" :-> <++>(),
        "emptyArr" :- <::>(),
        "objArr" :- <::>(
          JObjectOf(
            "title" :- "The man who changed china.",
            "list" :- <::>((1 to 10).map(x => JObjectOf(x.toString :- x)):_*)
          )
        )
      )
    ), "d" :- 1, "arr":- JArrayOf(1, "2", 3, <::>(4, "5", false)))
    val i = 10.0
    val njs =
      naiveJSON_!!"""
              {}
               """
    println("njs=", njs)
    println(sample)
    json >>! sample.toString
    println(parse[Int]("233").head)
    println(NaiveJSON.parseJSON("[]"))
    val s = sample.subVal_!!("main").subVal_!!("ns")
    println("arr1" + sample.subVal_!!("arr").subVal_!!(1).getAs_!![Int])
    println(sample.as_!![JObject]("main"))
    println((~s).getAs[Double].get)
    //println(<::>(1,2,3))
    val listOfInt = "[" *> (int <|> ",") <* "]" scope "List of Int"
    listOfInt >>! "[1, 2, 3]"
  }

  def main(args: Array[String]): Unit = {
    testAST()
  }
}
