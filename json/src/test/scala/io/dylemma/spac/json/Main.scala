package io.dylemma.spac.json

import io.dylemma.spac.{Consumer, HandlerFactory, Splitter, debug, json}

object Main {

	val helloWorldJson =
		"""{
		  |  "hello": [
		  |    {"a": 1 },
		  |    "wtf is this",
		  |    [1, 2, 3],
		  |    true
		  |  ],
		  |  "world": null
		  |}""".stripMargin

	// ADT representing the kinds of things in the `hello` array
	sealed trait HelloItem
	object HelloItem {
		case class A(value: Int) extends HelloItem
		case class Arr(values: List[Int]) extends HelloItem
		case class Bool(value: Boolean) extends HelloItem
		case class Str(value: String) extends HelloItem
		case class AEvents(events: List[JsonEvent]) extends HelloItem

		implicit val helloParser = JsonParser.oneOf(
			JsonSplitter("a").first[Int].map(A),
			JsonParser.listOf[Int].map(Arr),
			JsonParser[String].map(Str),
			JsonParser[Boolean].map(Bool)
		)
	}

	// case class representing a "hello world" object
	case class HelloWorld(
		hello: List[HelloItem],
		world: Option[String]
	)
	object HelloWorld {
		implicit val helloWorldParser: JsonParser[HelloWorld] = (
			JsonSplitter("hello" \ anyIndex).asListOf[HelloItem] and
			JsonSplitter("world").first(JsonParser.nullable[String])
		).as(HelloWorld.apply)
	}


	def main(args: Array[String]): Unit = {
		debug.enabled.set(false)

		println(JsonParser[HelloWorld] parse helloWorldJson)
	}

	// Below here is just some brainstorming for how JSON parsers/splitters should
	// be put together in order to handle certain input scenarios.

	/*
	Parser.forInt
	 */
	val j1 =
		"""1"""

	/*
	Parser.forString
	 */
	val j2 =
		""""hello""""

	/*
	Parser.forNull
	-
	Parser.optional(Parser.forInt)
	-
	Parser.forInt.orNull // maybe?
	 */
	val j3 = "null"

	/*
	JsonSplitter(inArray).asListOf(Parser.forInt)
	// inArray = atIndex(_ => true)
	 */
	val j4 = "[1, 2, 3]"

	/*
	(
		JsonSplitter(atIndex(0)).first[Int] ~
		JsonSplitter(atIndex(1)).first[String]
	).asTuple
	 */
	val j5 =
		"""[1, "hello"]"""

	/*
	JsonSplitter(inObject)
	-
	Parser.fieldOpt("a", Parser.forInt)
	 */
	val j6 =
		"""{ }"""

	/*
	Parser.field("a", Parser.forInt)
	-
	JsonSplitter("a").first[Int]
	 */
	val j7 =
		"""{ "a": 1 }"""

	/*
	Parser.forDouble
	 */
	val j8 =
		"""1.234"""

	/*
	Parser.forBool
	 */
	val j9 =
		"""true"""

	/*
	JsonSplitter("a" \ "b" \ atIndex(1)).first[String]
	 */
	val j10 =
		"""{
		  |  "a": {
		  |    "b": [null, "important info", null]
		  |  }
		  |}
		""".stripMargin
}
