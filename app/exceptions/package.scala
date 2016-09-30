import tags._
import play.api.libs.json.{ Json, JsPath }
import play.api.data.validation.ValidationError
import play.api.mvc.Results.BadRequest

package exceptions {

  sealed trait UnexpectedState {
    def message: String = this match {
      case NotFound(id) => s"Id $id not found"
      case JsonError(error) => s"Json error: $error"
    }

    def toResult = {
      BadRequest(Json.obj("error" -> this.message))
    }
  }

  case class NotFound[A](id: Id[A]) extends UnexpectedState
  case class JsonError(error: Seq[(JsPath, Seq[ValidationError])]) extends UnexpectedState
}
