import java.net.URL

import moe.roselia.NaiveJSON
import moe.roselia.NaiveJSON.dynamicJSON._
import moe.roselia.NaiveJSON.parser._
import moe.roselia.NaiveJSON.DSL._
import moe.roselia.NaiveJSON.JSON

import scala.beans.BeanProperty
import scala.io.Source

class NaiveStruct(x: Int) {
  @BeanProperty var y: Int = x + 1

  def wsp = x

  def wsp_=(s: String): Unit = {
    println("No fucking way, " + s)
  }
}

class Person(val name: String, val age: Int, val gender: Boolean, val pet: Pet)

case class Pet(name: String, tp: String)

object RoseliaBlog {
  def structor(): Unit = {
    val n = new NaiveStruct(0)
    println(n.y)
    n.wsp = "233"
  }

  def `testAST!`(): Unit = {
    val sample = JObjectOf("main" :- (
      "number" :- 123,
      "double" :- 233.0,
      "shouldBeDouble" :- 1234123423451234342134D,
      "bool" :- true,
      "null" :- JNull,
      "ns" :- "233",
      "obj" :- (
        "null" :- JNull,
        "a" :- "a",
        "arr" :- JArrayOf(JNull, false),
        "emptyObj" :-> <++>(),
        "emptyArr" :- <::>(),
        "objArr" :- <::>(
          JObjectOf(
            "title" :- "The man who changed china.\"\r\nHIM\"\\",
            "list" :- <::>((1 to 10).map(x => JObjectOf(x.toString :- x)): _*)
          )
        )
      )
    ), "d" :- 1, "arr" :- JArrayOf(1, "2", 3, <::>(4, "5", false)))
    val i = 10.0
    val njs =
      naiveJSON_!!"""
              [1,2,{}]
               """
    println("njs=", njs)
    val arr = sample.toDynamic.main.obj.subVal("arr")
    IO.inspect(arr)
    sample("arr")(2).get
    try {
      val str = sample("main")("bool").get.as_!![Double]
      println("There should be an Exception, " + str)
      //if(str.isDefined) throw new IllegalStateException("Last attempt should failed")
    } catch {
      case e: ClassCastException => println("Yes!", e)
    }
    println(sample.format)
    json >>! sample.format
    println(parse[Int]("233").head)
    println(NaiveJSON.parseJSON("[]"))
    val s = sample("main")("obj")("arr").as[JArray].get
    println("arr1" + sample.subVal_!!("arr").subVal_!!(1).getAs_!![Int])
    println(sample.as_!![JObject]("main"))
    println("LIST AS DOUBLE" + s)
    //println(<::>(1,2,3))
    val listOfInt = "[" *> (int <|> ",") <* "]" scope "List of Int"
    listOfInt >>! "[1, 2, 3]"
    (json >>! "[1, 2, 3]").mapSuccess(s => println(s.format))

  }

  def main(args: Array[String]): Unit = {
    val st = System.currentTimeMillis()
    //`testAST!`()
    //structor()
    //testMacro()
    //testEscape()
    //`escaped?`()
    testReflect()
    val ed = System.currentTimeMillis()
    IO.inspect("Finish in", ed - st, "ms")
  }

  def testEscape(): Unit = {
    val url = new URL("http://localhost:5000/api/post/1")
    val es = Source.fromURL(url, "utf8").mkString
    val js = "\"\n\""
    IO.inspect(es)
    json >>! es
  }

  def `escaped?`(): Unit = {
    val need = "\r\n\\\""
    IO.inspect(need.toEscapedQuoted)
  }

  def testReflect(): Unit = {
    val person = new Person("WSP", 20, true, Pet("Doge", "dog"))

    val fcls = NaiveJSON.reflect.fromPlainClass(person, {
      case "name" => Some("fullName")
      case x => Some(x)
    })
    IO inspect NaiveJSON.reflect.fromPlainClass(person.pet)
    //val fclsM = NaiveJSON.Macros.ClassLoader.fromPlainClass(person)
    val smp = ("fullName" :- person.name) <+> ("age":- person.age) <+> ("gender":- person.gender) <+>
      ("pet" :- (
        "name":- person.pet.name,
        "tp":- person.pet.tp))
    val altDSL = JObjectOf (
      "fullName" :- person.name,
      "age" :- person.age,
      "gender" :- person.gender,
      "pet" :- (
        "name":- person.pet.name,
        "tp":- person.pet.tp
      )
    )
    println(fcls.format)
    IO inspect altDSL
    IO.inspect("True = ", smp == fcls, smp == altDSL)
    val dynm = fcls.toDynamic
    IO.inspect("Person:", dynm.fullName.getVal_!!, "Age:", dynm.age !!, "has a", dynm.pet.tp !!, "named", dynm.pet.name !!)
  }

  object IO {
    def puts: Any => Unit = println

    def inspect(any: Any*): Unit = {
      println(any.mkString(" "))
    }
  }

}
