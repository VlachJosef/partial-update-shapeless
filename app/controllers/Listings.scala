package controllers

import play.api.mvc.{ Action, Controller }
import play.api.libs.json._
import scala.concurrent.Future
import cats.data.{ Xor, XorT }

import model.ListingReq
import service.ListingService
import tags._
import cats.implicits._
import model._
import exceptions._
import core.ServiceResponse

import scala.concurrent.ExecutionContext.Implicits.global

import core.PartialReader

class Listings(listingService: ListingService) extends Controller {

  def overview = Action.async { implicit request =>
    listingService.all.fold(
      error => error.toResult,
      listings => Ok(Json.toJson(listings))
    )
  }

  def save = Action.async(parse.json) { implicit request =>
    request.body.validate[ListingReq].fold(
      invalid = e => Future.successful(BadRequest(JsError.toJson(e))),
      valid = { listingReq =>
      listingService.create(listingReq.listing).fold(
        error => error.toResult,
        listingId => Ok(Json.toJson(listingId))
      )
    }
    )
  }

  def byId(listingId: ListingId) = Action.async { implicit request =>
    listingService.byId(listingId).fold(
      error => error.toResult,
      listing => Ok(Json.toJson(listing))
    )
  }

  def delete(listingId: ListingId) = Action.async { implicit request =>
    listingService.delete(listingId).fold(
      error => error.toResult,
      _ => Ok
    )
  }

  def JsResultToXorT[A](in: JsResult[A]): ServiceResponse[A] = {
    in match {
      case JsError(errors) => XorT(Future.successful(Xor.left(JsonError(errors))))
      case JsSuccess(x, _) => XorT(Future.successful(Xor.right(x)))
    }
  }

  def update(listingId: ListingId) = Action.async(parse.json) { implicit request =>

    val listingReqOptReads = PartialReader[ListingReq](request.body)

    val updatedFieldsRes = (__ \ 'listing).json.pick[JsObject].reads(listingReqOptReads)

    val updatedFields: JsObject = updatedFieldsRes match {
      case JsSuccess(updatedFields, _) => updatedFields
      case JsError(_) => Json.obj()
    }

    (for {
      listing <- listingService.byId(listingId)
      listingUpd <- updateListing(listing, updatedFields)
      _ <- listingService.save(listingId, listingUpd)
    } yield listingUpd).fold(
      error => error.toResult,
      listing => Ok(Json.toJson(listing))
    )
  }

  private def updateListing(listing: Listing, updatedFields: JsObject): ServiceResponse[Listing] = {
    val listingAsJson = Listing.format.writes(listing)
    val updatedListing = listingAsJson.deepMerge(updatedFields)
    val listingUpd = Json.fromJson[Listing](updatedListing)
    JsResultToXorT(listingUpd)
  }
}
