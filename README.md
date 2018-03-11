# NaiveJSON

![NaiveJSON](https://github.com/Somainer/NaiveJSON/raw/master/naivejson.png)

A functional JSON parser, with a naive DSL.

一个函数式的JSON解析器，同时可以以一种神奇的方式构建JSON

## Before

NaiveJSON中的方法，后面是一个感叹号(!)的意味着该方法具有一定的副作用（通常是输出文本）;
后面是两个感叹号(!!)的意味着有可能抛出异常。当然，如果其本身返回的就是`Unit`就不受该约定的限制，因为这本身就意味着该方法具有副作用。

如果您遇到了不是这样的情况，可能是我吃错药了。

## API
### NaiveJSON.JSON
一个简单的JSON分析库，和一些隐式转换。

最简单的使用：

`NaiveJSON.parseJSON_!!(s: String): JSON`

`NaiveJSON.parseJSON(s: String): Either[List[(Int, String)], JSON]`

`List[(Int, String)]` 是错误的地址（错误发生的字符位置（从1开始））和提示

`JSON` 是一个特征类，里面可以是`JSONStruct`里面的：

```Scala
  trait JSON
  case class JDouble(get: Double) extends JSON 
  case class JInt(get: Int) extends JSON 
  case class JString(get: String) extends JSON 
  case class JBool(get: Boolean) extends JSON
  case class JArray(get: IndexedSeq[JSON]) extends JSON
  case class JObject(get: Map[String, JSON]) extends JSON 
  case object JNull extends JSON
```

一个JSON对象有以下方法
```Scala
  def as[T]
  def as_!![T]
  def getVal: Any
  def getAs[T]: Option[T]
  def getAs_!![T]: T
  def subVal(key: Int | String): Option[JSON]
  def subVal_!!(key: Int | String): JSON
  def toString: String
```
如果一个对象是JObject或JArray，subVal在对应的键值才会发生作用，否则总是返回None，!!总是抛出错误
在subVal可用时，会有apply方法，作用相同。

如果你确定了JSON的类别，可以将其转换，通过`as[T]`，例如 `j.as[JObject]`

在导入了NaiveDSL之后，`Option[JSON]` 将被隐式转换为MaybeJSON，从而subVal和apply方法可以被链式调用。

`json(key1)(key2)(key3) => Option[JSON]`

`json.subVal(key1).subVal(key2).subVal(key3) => Option[JSON]`

注意： `JString("233").getAs[Int] = Some(233: Int)`

### Dynamic

NaiveJSON目前支持动态访问JSON内的属性，这是实现将其映射为对象之前的妥协。

要想使用，请导入`DynamicJSON`

```Scala
import moe.roselia.NaiveJSON.dynamicJSON._
```

假设我们现在有一个JSON对象：json

如果我们想要其中的"foo"属性，只需`val foo = json.foo`，这个操作像MaybeJSON一样，支持链式调用

最后，调用`foo.toOption`，就转换为了`Option[JSON]`

### 使用Parser
NaiveJSON.JSON 中有特定的解析器: Parser
`type Parser[+T] = ParseState => Result[T]`
目前不需要关心`PaserState`，先关心`trait Result[T]`
```Scala
case class Success[+T](get: T, charsConsumed: Int) extends Result[T]
case class Failure(get: ParseError, isCommitted: Boolean) extends Result[Nothing]
```
Result[T] 的函数有（不限于）：
```Scala
def getOption : Option[T]
def get_!! : T
def getOrElse[B >: T](els: B): B
def getEither: Either[List[(Int, String)], T]
```
功能就是字面上的意思

Parser 是一个比较甜的字符串解析器，拥有以下API

当然，为了使用到完整的功能，请将其导入：`import moe.roselia.NaiveJSON.JSON._`

当导入这个时，以下会在必要时进行隐式转换：

    String => Parser[String]

    Regex => Parser[String]

一个Parser有以下方法：

p1:Parser[A], p2: Parser[B]

p1 | p2 先匹配p1，若失败，再匹配p2

p1 ** p2 同时匹配，若成功，返回(A, B)

map[B](f: A => B)： Parser[B]

map2[B, C](b: Parser[B])(f: (A, B) => C): Parser[C]

flatMap[B](f: A => Parser[B]): Parser[B] 

label(msg: String): Parser[A] 发生错误时，丢弃顶部信息，在栈底添加信息

scope(msg: String): Parser[A] 发生错误时，在栈顶添加消息

slice: Parser[String] 分析成功时，返回输入的字符串（而不是分析到的结果）

token: 忽略p1左右的空白字符

p1 *> p2 忽略 p2 之前的p1

p1 <* p2 忽略 p1 之前的p2

p1 <=> a 返回一个新的Parser，将匹配到p1的字符串将被映射成a

p1 ⇔ a 同上（Scala能用这个字符好神奇）

p1 <|> p2 = Parser[List[A]] 返回由p2分割的所有p1

#### 例子：

匹配Boolean的Parser：
```Scala
  val bool = ("true" ⇔ true) | ("false" ⇔ false)
```

匹配一个List[Int]:
```Scala
  val int = "^(0|[1-9][0-9]*|-[1-9][0-9]*)".r.token map (_.toInt) label "Hey, I need an Int!")
  val listOfInt = "[" *> (int <|> ",") <* "]" scope "List of Int"
```

在`NaiveJSON.JSON`中预定义的JSON相关的解析器

nil = "null" <=> JNull

jbool = bool map JBool

literal : JSON Literals

keyVal: "{String}": {Literals} => (String, JSON)

jObject: Parser[JObject]

json: Parser[JSON]

测试：
```Scala
  bool >> "true" // => Success(true, 4)
  bool >> "yes" // => Failure(ParseError(List((Location(yes,0),Expect String: "false"))),false)
```
当然，在REPL中，用另外一个方法比较友好

    scala> bool >>! "yes"
    at: Line1
    in #1: Expect String: "false"
    scala> bool >>! "true"
    Success! got: true

当然，这两者的返回值是相同的，区别是其中一个会打印结果

## DSL
NaiveJSON 有一套DSL（NaiveDSL）用于构建JSON

同样，需要导入：`import moe.roselia.NaiveJSON.NaiveDSL._`

```Scala
val sample = JObjectOf ("main" :- (
  "number" :- 123,
  "double" :- 233.0,
  "shouldBeDouble" :- 1234123423451234342134D,
  "bool" :- true,
  "null" :- JNull,
  "hahaha":- "233",
  "obj" :- (
    "arr" :- JArrayOf(JNull, false),
    "emptyObj" :- <++>(), // <++> is JObjectOf
    "emptyArr" :- <::>(), // <::> is JArrayOf
    "intToString" :- <:>((1 to 10).map(x => JObjectOf(x.toString :- x)))
    // <:>(xs: Seq[JSON]) = <::>(xs:_*)
  )
), "d" :- 1, "arr":- JArrayOf(1, "2", "tic", 4, "toe"))
```

注意：这里`null`必须用`JNull`代替，否则会抛出NPE，目前没有想到什么优雅的解决方案。

如果你觉得这样太麻烦，而且你也不嫌弃速度，你可以使用字符串插值器：

```Scala
import moe.roselia.NaiveJSON.JSON._
val njs =
  naiveJSON_!!"""
              {"a": ${1+1}}
            """
```
插值器

naiveJSON: Option[JSON]

naiveJSON_!!: JSON

注意：这样会在运行时抛出异常，而不会在编译时就检查。

### JSON 转换为 String

对于一个JSON对象(json: JSON)，有若干方法将其转换为字符串。

`json.toString`: 转换为紧凑的字符串

`json.format(indent: Int)`: 格式化JSON，以`indent`个空格进行缩进

`json.format`: 作用同`json.format(2)`

### Class 转换为 JSON

一个对象（包括class，case class）都可以被转换为JSON

`NaiveJSON.reflect.fromPlainClass[T](obj: T, objMapper: String => Option[String]): JSON`

`obj` 被转换对象

`objMapper` 将对象的属性名转换为新的名字，如果是`None`则不会包含这个属性

例如，现有这么一个class

```Scala
case class Person(name: String, age: Int) //A top-level class
```

```Scala
val person = Person("Elder", 91)
val personJSON = 
  NaiveJSON.reflect.fromPlainClass(person, {
    case "name" => Some("fullName")
    case x => Some(x)
  })
println(personJSON.format)
```

结果：

```JSON
{
  "fullName": "Elder",
  "age": 91
}
```

如果你想要更复杂的功能，还是DSL更适合

## P.S.
为了方便，在Test中我写了很多带感叹号的方法，尾缀_!!可能让你看着难受，写着也难受，这正是我的目的，NaiveJSON提供了很多Option和Either来解决错误问题。NaiveJSON更倾向于使用函数式的方法解决异常问题。