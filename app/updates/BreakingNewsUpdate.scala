package updates

import java.net.URI
import java.security.InvalidParameterException

import com.gu.mobile.notifications.client._
import com.gu.mobile.notifications.client.models.Importance.Importance
import com.gu.mobile.notifications.client.models.Topic._
import com.gu.mobile.notifications.client.models.TopicTypes.Breaking
import com.gu.mobile.notifications.client.models._
import conf.ApplicationConfiguration
import org.apache.commons.lang3.StringEscapeUtils
import play.api.Logger
import play.api.libs.json.Json
import play.api.libs.ws.{WSAPI, WSResponse}
import play.api.mvc.Result
import play.api.mvc.Results.{InternalServerError, Ok}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

class InvalidNotificationContentType(msg: String) extends Throwable(msg) {}

class BreakingNewsUpdate(val config: ApplicationConfiguration, val ws: WSAPI) {
  lazy val client = {
    Logger.info(s"Configuring breaking news client to send notifications to ${config.notification.host} and ${config.notification.legacyHost}")
    ApiClient(
      host = config.notification.host,
      apiKey = config.notification.key,
      httpProvider = new NotificationHttpProvider(ws),
      legacyHost = config.notification.legacyHost,
      legacyApiKey = config.notification.legacyKey
    )
  }

  def putBreakingNewsUpdate(
    collectionId: String,
    collection: ClientHydratedCollection,
    email: String
  ): Future[Result] = {
    val futurePossibleErrors = Future.traverse(collection.trails)(trail => sendAlert(trail, email))
    futurePossibleErrors.map { listOfPossibleErrors => {
      val errors = listOfPossibleErrors.flatten
      if (errors.isEmpty) Ok
      else InternalServerError(Json.toJson(errors))
    }}
  }

  private def sendAlert(trail: ClientHydratedTrail, email: String): Future[Option[String]] = {
    def handleSuccessfulFuture(result: Either[ApiClientError, Unit]) = result match {
      case Left(error) =>
        Logger.error(s"Error in breaking news: ${error.description}")
        Some(error.description)
      case Right(_) => None
    }
    def withExceptionHandling(block: => Future[Option[String]]): Future[Option[String]] = {
      Try(block) match {
        case Success(futureMaybeError) => futureMaybeError
        case Failure(t: Throwable) =>
          val message = s"Exception in breaking news client send for trail ${trail.headline} because ${t.getMessage}"
          Logger.error(message, t)
          Future.successful(Some(message))}
    }

    if (trail.alert.getOrElse(false)) {
      withExceptionHandling({
        Logger.info(s"Sending breaking news alert for trail $trail")
        client.send(createPayload(trail, email))
          .map(handleSuccessfulFuture)
          .recover {
            case NonFatal(e) => Some(e.getMessage)
          }
      })
    } else Future.successful(None)
  }

  private def createPayload(trail: ClientHydratedTrail, email: String): BreakingNewsPayload = {
    BreakingNewsPayload(
      message = StringEscapeUtils.unescapeHtml4(trail.headline),
      thumbnailUrl = trail.thumb.map{new URI(_)},
      sender = email,
      link = createLinkDetails(trail),
      imageUrl = trail.imageHide match {
        case Some(true) => None
        case _ => trail.image.map{new URI(_)}
      },
      importance = parseImportance(trail.group),
      topic =  parseTopic(trail.topic),
      debug = false
    )
  }

  private def createLinkDetails(trail: ClientHydratedTrail) = {
    if (trail.isArticle) {
      GuardianLinkDetails(
        contentApiId = trail.path.getOrElse(throw new InvalidParameterException(s"Missing content API id for ${trail.headline}")),
        title = trail.headline,
        git = GITContent,
        thumbnail = trail.thumb,
        shortUrl = trail.shortUrl
      )
    } else {
      throw new InvalidNotificationContentType("Impossible to send snap notifications")
    }
  }

  private def parseImportance(name: Option[String]): Importance = {
    name match {
      case Some("major") => Importance.Major
      case Some("minor") => Importance.Minor
      case Some("") => Importance.Minor
      case Some(importance) => throw new InvalidParameterException(s"Invalid importance $importance")
      case None => Importance.Minor
    }
  }

  private def parseTopic(topic: Option[String]): Set[Topic] = {
    topic match {
      case Some("global") => Set(BreakingNewsUk, BreakingNewsUs, BreakingNewsAu, BreakingNewsInternational)
      case Some("au") => Set(BreakingNewsAu)
      case Some("international") => Set(BreakingNewsInternational)
      case Some("uk") => Set(BreakingNewsUk)
      case Some("us") => Set(BreakingNewsUs)
      case Some("sport") => Set(BreakingNewsSport)
      case Some("") => throw new InvalidParameterException(s"Invalid empty string topic")
      case Some(notYetImplementedTopic) => Set(Topic(Breaking, notYetImplementedTopic))
      case None => throw new InvalidParameterException(s"Invalid empty topic")
    }
  }
}

class NotificationHttpProvider(val ws: WSAPI) extends HttpProvider {
  override def post(url: String, contentType: ContentType, body: Array[Byte]): Future[HttpResponse] = {
    ws.url(url)
      .withHeaders("Content-Type" -> s"${contentType.mediaType}; charset=${contentType.charset}")
      .post(body)
      .map(extract)
  }

  override def get(url: String): Future[HttpResponse] = ws.url(url).get().map(extract)

  private def extract(response: WSResponse): HttpResponse = {
    if (response.status >= 200 && response.status < 300) {
      Logger.info("Breaking news notification sent correctly")
      HttpOk(response.status, response.body)
    } else {
      Logger.error(s"Unable to send breaking news notification, status ${response.status}: ${response.statusText}")
      HttpError(response.status, response.body)
    }
  }
}
