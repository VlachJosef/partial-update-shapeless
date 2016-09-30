package model

import play.api.libs.json.Json

case class ListingReq(
  listing: Listing
)

object ListingReq {
  implicit val format = Json.format[ListingReq]
}
