package model

import play.api.libs.json.Json
import tags._

case class Listing(
  id: ListingId,
  contact: Contact,
  address: Address,
  location: Location
)

object Listing {
  implicit val format = Json.format[Listing]
}
