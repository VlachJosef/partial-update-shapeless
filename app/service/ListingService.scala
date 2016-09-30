package service

import cats.implicits._

import tags._
import model._
import repositories._
import play.api.libs.concurrent.Execution.Implicits._
import java.util.UUID
import core.ServiceResponse

class ListingService(
    contactRepository: ContactRepository,
    addressRepository: AddressRepository,
    locationRepository: LocationRepository
) {

  def all: ServiceResponse[List[Listing]] = {
    contactRepository.all.flatMap { contacts =>
      contacts.toList.map {
        case (listingId, contact) =>
          for {
            address <- addressRepository.byListingId(listingId)
            location <- locationRepository.byListingId(listingId)
          } yield {
            Listing(
              id = listingId,
              contact = contact,
              address = address,
              location = location
            )
          }
      }.sequenceU
    }
  }

  def byId(listingId: ListingId): ServiceResponse[Listing] = {
    for {
      contact <- contactRepository.byListingId(listingId)
      address <- addressRepository.byListingId(listingId)
      location <- locationRepository.byListingId(listingId)
    } yield Listing(
      id = listingId,
      contact = contact,
      address = address,
      location = location
    )
  }

  def create(listing: Listing): ServiceResponse[ListingId] = {

    val listingId: ListingId = shapeless.tag[ListingIdTag][UUID](UUID.randomUUID)

    save(listingId, listing)
  }

  def save(listingId: ListingId, listing: Listing): ServiceResponse[ListingId] = {
    for {
      _ <- contactRepository.save(listingId, listing.contact)
      _ <- addressRepository.save(listingId, listing.address)
      _ <- locationRepository.save(listingId, listing.location)
    } yield listingId
  }

  def delete(listingId: ListingId): ServiceResponse[Unit] = {
    for {
      _ <- contactRepository.delete(listingId)
      _ <- addressRepository.delete(listingId)
      _ <- locationRepository.delete(listingId)
    } yield ()
  }
}
