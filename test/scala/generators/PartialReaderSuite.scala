package generators

import org.scalatest.{ FlatSpec, Matchers }
import play.api.libs.json.{ Json, JsValue }

import core.PartialReader

case class TestShape1(x: Option[Int], y: Option[String])
case class TestShape2(x: Option[Int], y: String)
case class TestShape3(x: Int, y: String)

class PartialReaderSuite extends FlatSpec with Matchers {

  "PartialReader" should "handle empty json" in {
    val request = """{}"""
    val json = Json.parse(request)

    val result1: JsValue = PartialReader[TestShape1](json)
    val result2: JsValue = PartialReader[TestShape2](json)
    val result3: JsValue = PartialReader[TestShape3](json)

    result1 should be(Json.obj())
    result2 should be(Json.obj())
    result3 should be(Json.obj())
  }

  it should "get Int field defined in case class not regarding if field is mandatory or optional" in {
    val request = """{ "x" : 123 }"""
    val json = Json.parse(request)

    val result1: JsValue = PartialReader[TestShape1](json)
    val result2: JsValue = PartialReader[TestShape2](json)
    val result3: JsValue = PartialReader[TestShape3](json)

    result1 should be(Json.obj("x" -> 123))
    result2 should be(Json.obj("x" -> 123))
    result3 should be(Json.obj("x" -> 123))
  }

  it should "get String field defined in case class not regarding if field is mandatory or optional" in {
    val request = """{ "y" : "home" }"""
    val json = Json.parse(request)

    val result1: JsValue = PartialReader[TestShape1](json)
    val result2: JsValue = PartialReader[TestShape2](json)
    val result3: JsValue = PartialReader[TestShape3](json)

    result1 should be(Json.obj("y" -> "home"))
    result2 should be(Json.obj("y" -> "home"))
    result3 should be(Json.obj("y" -> "home"))
  }

  it should "get multiple fields defined in case class not regarding if field is mandatory or optional" in {
    val request = """|{
                     |  "x" : 123,
                     |  "y" : "home"
                     |}""".stripMargin
    val json = Json.parse(request)

    val result1: JsValue = PartialReader[TestShape1](json)
    val result2: JsValue = PartialReader[TestShape2](json)
    val result3: JsValue = PartialReader[TestShape3](json)

    val expectedJson = Json.obj(
      "x" -> 123,
      "y" -> "home"
    )

    result1 should be(expectedJson)
    result2 should be(expectedJson)
    result3 should be(expectedJson)
  }

  it should "ignore unmapped fields" in {
    val request = """|{
                     |  "a" : 123,
                     |  "b" : "home"
                     |}""".stripMargin
    val json = Json.parse(request)

    val result1: JsValue = PartialReader[TestShape1](json)
    val result2: JsValue = PartialReader[TestShape2](json)
    val result3: JsValue = PartialReader[TestShape3](json)

    result1 should be(Json.obj())
    result2 should be(Json.obj())
    result3 should be(Json.obj())
  }
}
