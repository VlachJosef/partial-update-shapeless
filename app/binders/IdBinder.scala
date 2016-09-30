package binders

import java.util.UUID
import play.api.mvc.PathBindable
import shapeless.tag
import scala.util.{ Failure, Success, Try }

import tags.IdParam

object IdBinder {
  implicit def idBinder[A](implicit stringBinder: PathBindable[String]) =
    new PathBindable[IdParam[A]] {
      def bind(key: String, value: String): Either[String, IdParam[A]] = {
        for {
          idValue <- stringBinder.bind(key, value).right
          id <- parseIdString[A](idValue).right
        } yield id
      }

      override def unbind(key: String, id: IdParam[A]): String =
        stringBinder.unbind(key, id.toString)
    }

  private[binders] def parseIdString[A](idString: String): Either[String, IdParam[A]] = {
    Try(UUID.fromString(idString)) match {
      case Failure(e) => Left(e.getMessage)
      case Success(uuid) => Right(IdParam[A](tag[A][UUID](uuid)))
    }
  }
}
