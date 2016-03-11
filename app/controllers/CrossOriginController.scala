package controllers

import com.gu.facia.client.models.FrontJson
import model.Cors
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import services.ConfigAgent

import scala.concurrent.Future


object CrossOriginController extends Controller {
  object SlimFrontJson {
    implicit val jsonFormat = Json.format[SlimFrontJson]
  }
  case class SlimFrontJson (priority: String)

  def listFronts() = Action.async { implicit request =>
    def formatFront (front: FrontJson): SlimFrontJson = SlimFrontJson(front.priority.getOrElse("editorial"))
    val response = ConfigAgent.getMasterConfig.map(_.fronts.mapValues(formatFront)) match {
      case Some(fronts) => Ok(Json.toJson(fronts))
      case None => ServiceUnavailable
    }
    Future.successful(Cors(response, Some("GET")))
  }
}
