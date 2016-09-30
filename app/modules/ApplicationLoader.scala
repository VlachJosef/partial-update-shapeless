package modules

import play.api.ApplicationLoader.Context
import play.api.{ BuiltInComponentsFromContext, BuiltInComponents }
import play.api.routing.Router
import router.Routes
import service.ListingService
import controllers.Listings
import repositories._
import org.scalacheck.rng.Seed

class ApplicationLoader extends play.api.ApplicationLoader {
  def load(context: Context) = {
    (new BuiltInComponentsFromContext(context) with ApplicationModule).application
  }
}

// Manual DI
trait ApplicationModule extends BuiltInComponents {

  implicit val seed = Seed(System.currentTimeMillis)

  lazy val contactRepository = new ContactRepository()
  lazy val addressRepository = new AddressRepository()
  lazy val locationRepository = new LocationRepository()
  lazy val listingService = new ListingService(contactRepository, addressRepository, locationRepository)

  lazy val listingController = new Listings(listingService)

  lazy val router: Router = new Routes(httpErrorHandler, listingController, "/")
}
