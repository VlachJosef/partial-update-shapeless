import org.scalacheck.Gen
import scala.collection.mutable.Map
import model._
import tags._
import org.scalacheck.rng.Seed
import java.util.UUID

package object generators {

  private val listingIdGen = Gen.uuid.map(uuid => shapeless.tag[ListingIdTag][UUID](uuid))

  private lazy val addressGen: Gen[Address] = for {
    address <- Gen.oneOf("address 1", "address 2", "address 3")
    postalCode <- Gen.oneOf("1011", "2314", "9762", "2332", "0124")
    countryCode <- Gen.oneOf("US", "UK", "DE", "JA", "FR")
    city <- Gen.oneOf("Austin", "Vienna", "Washington", "London", "Paris")
    state <- Gen.oneOf("AL", "AK", "AS", "AZ", "AR", "CA", "CO", "CT", "DE", "DC", "FL", "GA", "GU", "HI", "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MD", "MH", "MA", "MI", "FM", "MN", "MS", "MO", "MT", "NE", "NV", "NH", "NJ", "NM", "NY", "NC", "ND", "MP", "OH", "OK", "OR", "PW", "PA", "PR", "RI", "SC", "SD", "TN", "TX", "UT", "VT", "VA", "VI", "WA", "WV", "WI", "WY")
    country <- Gen.oneOf("United States", "UK", "Germany", "Japan")
  } yield Address(
    address = address,
    postalCode = postalCode,
    countryCode = countryCode,
    city = city,
    state = state,
    country = country
  )

  private lazy val locationGen: Gen[Location] = for {
    lat <- Gen.oneOf(0.0, 0.1, 0.2)
    lng <- Gen.oneOf(1.0, 1.1, 1.2)
  } yield Location(
    lat = lat,
    lng = lng
  )

  private lazy val contactGen: Gen[Contact] = for {
    formattedPhone <- Gen.oneOf("0306-999-0682", "0191-498-0001", "0151-496-0741")
    phone <- Gen.const(formattedPhone.replaceAll("-", ""))
  } yield Contact(
    phone = phone,
    formattedPhone = formattedPhone
  )

  private lazy val listingGen: Gen[(ListingId, Contact, Address, Location)] = for {
    listingId <- listingIdGen
    contact <- contactGen
    address <- addressGen
    location <- locationGen
  } yield (listingId, contact, address, location)

  lazy val contactsGen: Gen[Map[ListingId, Contact]] =
    Gen.buildableOfN[Map[ListingId, Contact], (ListingId, Contact)](10, listingGen.map { case (listingId, contacts, _, _) => (listingId, contacts) })

  lazy val addresssGen: Gen[Map[ListingId, Address]] =
    Gen.buildableOfN[Map[ListingId, Address], (ListingId, Address)](10, listingGen.map { case (listingId, _, addresses, _) => (listingId, addresses) })

  lazy val locationsGen: Gen[Map[ListingId, Location]] =
    Gen.buildableOfN[Map[ListingId, Location], (ListingId, Location)](10, listingGen.map { case (listingId, _, _, locations) => (listingId, locations) })

  implicit class GenExtension[A](gen: Gen[A])(implicit seed: Seed) {
    def sampleWithSeed: Option[A] = {
      gen(Gen.Parameters.default, seed)
    }
  }
}
