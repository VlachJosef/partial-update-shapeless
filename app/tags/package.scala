import shapeless.tag.@@
import java.util.UUID
import play.api.libs.json.{ Writes, Reads, JsResult, JsValue, JsString, JsSuccess, JsError }
import scala.util.Try

import scala.language.implicitConversions

package tags {
  trait ListingIdTag

  case class IdParam[T](value: Id[T])

  object IdParam {
    implicit def toId[T](idParam: IdParam[T]): Id[T] = idParam.value
  }
}

package object tags {
  type Id[T] = UUID @@ T

  type ListingId = UUID @@ ListingIdTag

  implicit def readId[T]: Reads[Id[T]] = new Reads[Id[T]] {
    def reads(json: JsValue): JsResult[Id[T]] = json match {
      case GetUUID(uuid) => JsSuccess(shapeless.tag[T][UUID](uuid))
      case unknown => JsError(s"UUID value expected, got: $unknown")
    }
  }

  implicit def writesId[T]: Writes[Id[T]] = new Writes[Id[T]] {
    def writes(uuid: Id[T]): JsValue = JsString(uuid.toString)
  }

  private object GetUUID {
    def unapply(json: JsValue): Option[UUID] = {
      json match {
        case JsString(uuid) => Try(UUID.fromString(uuid)).toOption
        case _ => None
      }
    }
  }
}
