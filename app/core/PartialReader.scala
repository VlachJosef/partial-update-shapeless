package core

import shapeless.tag.@@
import java.util.UUID

import shapeless._
import labelled._
import play.api.libs.json._

trait ToOptionConverter[V] extends DepFn1[V]

trait LowPriorityToOptionConverter {
  implicit def base[V] = new ToOptionConverter[V] {
    type Out = Option[V]
    def apply(v: V): Out = Option(v)
  }
}

object ToOptionConverter extends LowPriorityToOptionConverter {
  implicit def option[V] = new ToOptionConverter[Option[V]] {
    type Out = Option[V]
    def apply(v: Option[V]) = v
  }
}

trait LabelledLiftToOption[R <: HList] extends DepFn1[R] { type Out <: HList }

trait LowPriorityLabelledLiftToOption {

  type Aux[R <: HList, Out0 <: HList] = LabelledLiftToOption[R] { type Out = Out0 }

  implicit def headNonLabelledGenericLiftToOption[K <: Symbol, V, T <: HList](
    implicit
    dht: Lazy[LabelledLiftToOption[T]],
    toOptionConv: ToOptionConverter[V]
  ): Aux[FieldType[K, V] :: T, FieldType[K, toOptionConv.Out] :: dht.value.Out] = new LabelledLiftToOption[FieldType[K, V] :: T] {
    type Out = FieldType[K, toOptionConv.Out] :: dht.value.Out
    def apply(r: FieldType[K, V] :: T) = field[K](toOptionConv(r.head)) :: dht.value(r.tail)
  }
}

object LabelledLiftToOption extends LowPriorityLabelledLiftToOption {

  implicit object hnilLiftToOption extends LabelledLiftToOption[HNil] {
    type Out = HNil
    def apply(r: HNil) = HNil
  }

  implicit def headLabelledGenericLiftToOption[K <: Symbol, V, R <: HList, T <: HList](
    implicit
    gen: LabelledGeneric.Aux[V, R],
    dhh: Lazy[LabelledLiftToOption[R]],
    dht: Lazy[LabelledLiftToOption[T]]
  ): Aux[FieldType[K, V] :: T, FieldType[K, Option[dhh.value.Out]] :: dht.value.Out] = new LabelledLiftToOption[FieldType[K, V] :: T] {
    type Out = FieldType[K, Option[dhh.value.Out]] :: dht.value.Out
    def apply(r: FieldType[K, V] :: T): Out = {
      val head = field[K](Option(dhh.value(gen.to(r.head))))
      head :: dht.value(r.tail)
    }
  }

  implicit def headLabelledGenericOptionLiftToOption[K <: Symbol, V, R <: HList, T <: HList](
    implicit
    gen: LabelledGeneric.Aux[V, R],
    dhh: Lazy[LabelledLiftToOption[R]],
    dht: Lazy[LabelledLiftToOption[T]]
  ): Aux[FieldType[K, Option[V]] :: T, FieldType[K, Option[dhh.value.Out]] :: dht.value.Out] = new LabelledLiftToOption[FieldType[K, Option[V]] :: T] {
    type Out = FieldType[K, Option[dhh.value.Out]] :: dht.value.Out
    def apply(r: FieldType[K, Option[V]] :: T): Out = {
      val head = field[K]((r.head: Option[V]).map(v => dhh.value(gen.to(v))))
      head :: dht.value(r.tail)
    }
  }

  def apply[R <: HList](implicit dh: LabelledLiftToOption[R]): Aux[R, dh.Out] = dh
}

trait ToJsValue[V] {
  def apply(v: V): JsValue
}

object ToJsValue {
  implicit def StringToJsonValue = new ToJsValue[String] {
    def apply(s: String) = JsString(s)
  }

  implicit def IntToJsonValue = new ToJsValue[Int] {
    def apply(i: Int) = JsNumber(i)
  }

  implicit def DoubleToJsonValue = new ToJsValue[Double] {
    def apply(i: Double) = JsNumber(i)
  }

  implicit def UUIDTagToJsonValue[A] = new ToJsValue[UUID @@ A] {
    def apply(i: UUID @@ A) = JsString(i.toString)
  }
}

trait ToJson[R <: HList] {
  def apply(j: JsValue): JsObject
}

object ToJson {
  implicit object hnilToJson extends ToJson[HNil] {
    def apply(j: JsValue): JsObject = Json.obj()
  }

  implicit def vValueToJson[K <: Symbol, V, T <: HList](
    implicit
    vReads: Reads[V],
    toJsValue: ToJsValue[V],
    toJsonT: ToJson[T],
    w: Witness.Aux[K]
  ) = new ToJson[FieldType[K, Option[V]] :: T] {
    def apply(j: JsValue): JsObject = {

      val name = w.value.name
      val readsVOpt = (__ \ name).readNullable(vReads)

      val jsonV = readsVOpt.reads(j) match {
        case JsSuccess(Some(v), _) => Json.obj(name -> toJsValue(v))
        case _ => Json.obj()
      }

      jsonV ++ toJsonT(j)
    }
  }

  implicit def vHListToJson[K <: Symbol, V <: HList, T <: HList](
    implicit
    toJsonV: ToJson[V],
    toJsonT: ToJson[T],
    w: Witness.Aux[K]
  ) = new ToJson[FieldType[K, Option[V]] :: T] {
    def apply(j: JsValue): JsObject = {

      val name = w.value.name

      val jsonV = (__ \ name).json.pick.reads(j) match {
        case JsSuccess(jsonObj, _) => Json.obj(name -> toJsonV(jsonObj))
        case JsError(error) => Json.obj()
      }

      jsonV ++ toJsonT(j)
    }
  }

  def apply[R <: HList](r: R, j: JsValue)(implicit toJson: ToJson[R]) = toJson(j)
}

object PartialReader {
  def apply[A] = new PartialReaderHelper[A]
}

class PartialReaderHelper[A] {
  def apply[R <: HList, R2 <: HList](json: JsValue)(
    implicit
    gen: LabelledGeneric.Aux[A, R],
    lift: LabelledLiftToOption.Aux[R, R2],
    toJson: ToJson[R2]
  ): JsObject = {
    toJson(json)
  }
}
