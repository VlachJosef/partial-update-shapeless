package model

import play.api.libs.json.Json

case class Contact(
  phone: String,
  formattedPhone: String
)

object Contact {
  implicit val format = Json.format[Contact]
}
