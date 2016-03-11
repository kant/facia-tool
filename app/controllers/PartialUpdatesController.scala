package controllers

import auth.PanDomainAuthActions
import com.gu.contentapi.client.model.ItemResponse
import com.gu.contentapi.client.model.v1.Content
import com.gu.contentapi.client.parser.JsonParser
import com.gu.facia.client.models.{TrailMetaData, Trail, CollectionJson}
import com.gu.pandomainauth.model.User
import conf.Configuration
import frontsapi.model.UpdateActions
import metrics.CloudWatch.LoggingAsyncHandler
import metrics.FaciaToolMetrics
import model.Cors
import play.api.Play.current
import play.api.libs.json._
import play.api.libs.ws.WS
import play.api.mvc.{BodyParsers, Controller}
import services.{PressCommand, FaciaPress, FrontsApi}
import updates.{StreamUpdate, UpdatesStream, UpdateList, Update}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}


object PartialUpdateRequest {
  implicit val jsonFormat = Json.format[PartialUpdateRequest]
}
case class PartialUpdateRequest(
  meta: String,
  path: String,
  value: String
)

object CapiArticle {
  implicit val jsonFormat = Json.format[CapiArticle]
}

case class CapiArticle(
  id: String,
  path: String
)

object PartialUpdatesController extends Controller with PanDomainAuthActions {
  def editCollection(collectionId: String) = APIAuthAction.async(BodyParsers.parse.tolerantJson) { implicit request =>
    request.body.validate[PartialUpdateRequest] match {
      case JsSuccess(action, _) => {
        val maybeResponse = for {
          maybeTrailInCapi <- getArticleFromCapi(action.path)
          maybeCollectionJson <- FrontsApi.amazonClient.collection(collectionId)
        } yield {
          (maybeTrailInCapi, maybeCollectionJson) match {
            case (_, None) => Future.successful(NotFound)
            case (Right(trail), Some(collectionJson)) =>
              extractTrailFromCollection(collectionJson, trail) match {
                case Left(errorMessage) => Future.successful(Conflict(errorMessage))
                case Right(trailInCollectionJson) =>
                  applyPartialUpdate(collectionId, action, trail, collectionJson, trailInCollectionJson, request.user).map(_ => Ok)
              }
            case (Left(errorMessage), _) => Future.successful(BadGateway(errorMessage))
          }
        }
        maybeResponse.flatMap(x => x).map(Cors(_, Some("POST")))
      }
      case _ => Future.successful(Cors(BadRequest, Some("POST")))
    }
  }

  private def getArticleFromCapi(path: String): Future[Either[String, CapiArticle]] = {
    FaciaToolMetrics.ProxyCount.increment()
    val contentApiHost: String = Configuration.contentApi.contentApiLiveHost
    val requiredFields = "show-fields=internalPageCode"
    val url = s"$contentApiHost/$path?$requiredFields${Configuration.contentApi.key.map(key => s"&api-key=$key").getOrElse("")}"

    WS.url(url).get().map { response =>
      Try({
        JsonParser.parseItem(response.body)
      }) match {
        case Success(itemResponse) =>
          if (itemResponse.status == "ok") {
            val maybeCapiArticle = for {
              content <- itemResponse.content
              fields <- content.fields
              pageId <- fields.internalPageCode
            } yield {
              CapiArticle(s"internal-code/page/$pageId", path)
            }
            maybeCapiArticle match {
              case Some(article) => Right(article)
              case None => Left("Unable to find the article in CAPI")
            }
          } else {
            Left("Invalid content API response: " + itemResponse.status)
          }
        case Failure(_) => Left("Unable to parse content API response into an item")
      }
    }
  }

  private def extractTrailFromCollection(collectionJson: CollectionJson, trail: CapiArticle): Either[String, Trail] = {
    collectionJson.draft match {
      case Some(list) if list.nonEmpty => Left("The collection contains draft content")
      case _ =>
        collectionJson.live.find(t => t.id.equals(trail.id)) match {
          case None => Left("Cannot find the article in live collection")
          case Some(trailInCollectionJson) => Right(trailInCollectionJson)
        }
    }
  }

  private def applyPartialUpdate(
   collectionId: String,
   action: PartialUpdateRequest,
   trail: CapiArticle,
   collecttionJson: CollectionJson,
   trailInCollectionJson: Trail,
   identity: User
  ): Future[Option[CollectionJson]] = {
    println("apply")
    val updateMetadata = action.meta match {
      case "imageSrc" => Map(
        action.meta -> JsString(action.value),
        "imageReplace" -> JsBoolean(true)
      )
      case "customKicker" => Map(
        action.meta -> JsString(action.value),
        "showKickerCustom" -> JsBoolean(true),
        "showKickerTag" -> JsBoolean(false),
        "showKickerSection" -> JsBoolean(false)
      )
      case _ => Map(
        action.meta -> JsString(action.value)
      )
    }
    val update = Update(UpdateList(
      id = collectionId,
      item = trail.id,
      position = Some(trail.id),
      draft = false,
      live = true,
      after = None,
      itemMeta = Some(TrailMetaData(trailInCollectionJson.meta.getOrElse(TrailMetaData.empty).json ++ updateMetadata))
    ))
    println(update)

    val futureCollectionJson = UpdateActions.updateCollectionList(update.update.id, update.update, identity)
    futureCollectionJson.map { maybeCollectionJson =>
      val updatedCollections = maybeCollectionJson.map(update.update.id -> _).toMap
      val collectionIds = updatedCollections.keySet

      FaciaPress.press(PressCommand(
        collectionIds,
        live = true,
        draft = false
      ))

      if (updatedCollections.nonEmpty) {
        UpdatesStream.putStreamUpdate(StreamUpdate(update, identity.email))
        maybeCollectionJson
      } else
        None
    }
  }
}
