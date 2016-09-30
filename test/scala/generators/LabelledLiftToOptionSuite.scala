package generators

import org.scalatest.{ FlatSpec, Matchers }
import play.api.libs.json._

import shapeless._
import test._
import labelled._

import core._

class LabelledLiftToOptionSuite extends FlatSpec with Matchers {

  case class A(x: Int, y: Option[String])
  case class B(x: Option[A], y: A)
  case class C(b: B, a: A)
  case class D(a: A, b: Option[B])

  "LabelledLiftToOption" should "lift all field of case class into Option" in {

    type ARepr = FieldType[Witness.`'x`.T, Option[Int]] :: FieldType[Witness.`'y`.T, Option[String]] :: HNil
    type BRepr = FieldType[Witness.`'x`.T, Option[ARepr]] :: FieldType[Witness.`'y`.T, Option[ARepr]] :: HNil
    type CRepr = FieldType[Witness.`'b`.T, Option[BRepr]] :: FieldType[Witness.`'a`.T, Option[ARepr]] :: HNil
    type DRepr = FieldType[Witness.`'a`.T, Option[ARepr]] :: FieldType[Witness.`'b`.T, Option[BRepr]] :: HNil

    val gena = LabelledGeneric[A]
    val genb = LabelledGeneric[B]
    val genc = LabelledGeneric[C]
    val gend = LabelledGeneric[D]

    val lgena = LabelledLiftToOption[gena.Repr]
    typed[LabelledLiftToOption.Aux[gena.Repr, ARepr]](lgena)

    val lgenb = LabelledLiftToOption[genb.Repr]
    typed[LabelledLiftToOption.Aux[genb.Repr, BRepr]](lgenb)

    val lgenc = LabelledLiftToOption[genc.Repr]
    typed[LabelledLiftToOption.Aux[genc.Repr, CRepr]](lgenc)

    val lgend = LabelledLiftToOption[gend.Repr]
    typed[LabelledLiftToOption.Aux[gend.Repr, DRepr]](lgend)
  }

  "Updater.getReader" should "turn json" in {

    val aoReader = PartialReader[A]

    val aoStr = """{ "x" : 1, "y" : "str" }"""

    val aoJson = Json.parse(aoStr)

    val aoJsonRes: JsObject = aoReader(aoJson)

    aoJsonRes should be(aoJson)
  }

  it should "handle deeply nested structures 1" in {

    val aoStr = """{ "x" : 1, "y" : "str" }"""
    val aoStrXOnly = """{ "x" : 1 }"""
    val boReader = PartialReader[B]

    val boStr = s"""{ "x" : $aoStr, "y" : $aoStr }"""
    val boJson = Json.parse(boStr)
    val boJsonRes = boReader(boJson)

    boJsonRes should be(boJson)

    val boStr2 = s"""{ "x" : $aoStr }"""
    val boJson2 = Json.parse(boStr2)
    val boJsonRes2 = boReader(boJson2)

    boJsonRes2 should be(boJson2)

    val boStr3 = s"""{ "x" : $aoStrXOnly, "y": $aoStrXOnly }"""
    val boJson3 = Json.parse(boStr3)
    val boJsonRes3 = boReader(boJson3)

    boJsonRes3 should be(boJson3)
  }

  it should "handle deeply nested structures 2" in {
    val coReader = PartialReader[C]

    val aoStr = """{ "x" : 1, "y" : "str" }"""
    val boStr = s"""{ "x" : $aoStr, "y" : $aoStr }"""
    val coStr = s"""{ "b" : $boStr, "a" : $aoStr }"""

    val coJson = Json.parse(coStr)

    val coJsonRes = coReader(coJson)

    coJsonRes should be(coJson)
  }

  it should "handle deeply nested structures 3" in {
    val doReader = PartialReader[D]

    val aoStr = """{ "x" : 1, "y" : "str" }"""
    val boStr = s"""{ "x" : $aoStr, "y" : $aoStr }"""
    val doStr = s"""{ "a" : $aoStr, "b" : $boStr }"""

    val doJson = Json.parse(doStr)

    val doJsonRes = doReader(doJson)

    doJsonRes should be(doJson)
  }
}
