package repositories

import cats.data.{ Xor, XorT }
import scala.concurrent.Future
import tags._
import exceptions._
import model.Contact
import org.scalacheck.rng.Seed
import scala.collection.mutable.Map
import core.ServiceResponse
import generators._

class ContactRepository(implicit seed: Seed) {

  private lazy val contacts: Map[ListingId, Contact] = contactsGen.sampleWithSeed.getOrElse(Map.empty[ListingId, Contact])

  def all: ServiceResponse[Map[ListingId, Contact]] = XorT(Future.successful(Xor.right(contacts)))

  def byListingId(listingId: ListingId): ServiceResponse[Contact] = {
    XorT(Future.successful(Xor.fromOption(contacts.get(listingId), NotFound(listingId))))
  }

  def save(listingId: ListingId, contact: Contact): ServiceResponse[Unit] = {
    contacts(listingId) = contact
    XorT(Future.successful(Xor.right(())))
  }

  def delete(listingId: ListingId): ServiceResponse[Unit] = {
    val result = contacts.get(listingId) match {
      case None => Xor.left(NotFound(listingId))
      case Some(listing) =>
        contacts -= listingId
        Xor.right(())
    }
    XorT(Future.successful(result))
  }
}
