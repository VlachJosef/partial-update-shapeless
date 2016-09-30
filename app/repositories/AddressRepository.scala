package repositories

import cats.data.{ Xor, XorT }
import scala.concurrent.Future
import tags._
import exceptions._
import model.Address
import org.scalacheck.rng.Seed
import scala.collection.mutable.Map
import core.ServiceResponse
import generators._

class AddressRepository(implicit seed: Seed) {

  private lazy val addresses: Map[ListingId, Address] = addresssGen.sampleWithSeed.getOrElse(Map.empty[ListingId, Address])

  def byListingId(listingId: ListingId): ServiceResponse[Address] = {
    XorT(Future.successful(Xor.fromOption(addresses.get(listingId), NotFound(listingId))))
  }

  def save(listingId: ListingId, address: Address): ServiceResponse[Unit] = {
    addresses(listingId) = address
    XorT(Future.successful(Xor.right(())))
  }

  def delete(listingId: ListingId): ServiceResponse[Unit] = {
    val result = addresses.get(listingId) match {
      case None => Xor.left(NotFound(listingId))
      case Some(listing) =>
        addresses -= listingId
        Xor.right(())
    }
    XorT(Future.successful(result))
  }
}
