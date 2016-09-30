package model

import play.api.libs.json.Json

case class Address(
  address: String,
  postalCode: String,
  countryCode: String,
  city: String,
  state: String,
  country: String
)

object Address {
  implicit val format = Json.format[Address]
}
