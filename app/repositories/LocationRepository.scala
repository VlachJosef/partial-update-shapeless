package repositories

import cats.data.{ Xor, XorT }
import scala.concurrent.Future
import tags._
import exceptions._
import model.Location
import org.scalacheck.rng.Seed
import scala.collection.mutable.Map
import core.ServiceResponse
import generators._

class LocationRepository(implicit seed: Seed) {

  private lazy val locations: Map[ListingId, Location] = locationsGen.sampleWithSeed.getOrElse(Map.empty[ListingId, Location])

  def byListingId(listingId: ListingId): ServiceResponse[Location] = {
    XorT(Future.successful(Xor.fromOption(locations.get(listingId), NotFound(listingId))))
  }

  def save(listingId: ListingId, location: Location): ServiceResponse[Unit] = {
    locations(listingId) = location
    XorT(Future.successful(Xor.right(())))
  }

  def delete(listingId: ListingId): ServiceResponse[Unit] = {
    val result = locations.get(listingId) match {
      case None => Xor.left(NotFound(listingId))
      case Some(listing) =>
        locations -= listingId
        Xor.right(())
    }
    XorT(Future.successful(result))
  }
}
