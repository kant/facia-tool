package controllers

import auth.PanDomainAuthActions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.gu.scanamo.{Scanamo, Table}
import conf.ApplicationConfiguration
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc.Controller
import services.AwsEndpoints

object FrontPressRecord {
  implicit val jsonFormat = Json.format[FrontPressRecord]
}
case class FrontPressRecord (
 stageName: String,
 frontId: String,
 pressedTime: String,
 errorCount: Int,
 messageText: String,
 statusCode: String,
 actionTime: String
)

class PressController (val config: ApplicationConfiguration, awsEndpoints: AwsEndpoints, override val wsClient: WSClient) extends Controller with PanDomainAuthActions {
  private lazy val client = {
    val client = new AmazonDynamoDBClient(config.aws.mandatoryCredentials)
    client.setEndpoint(awsEndpoints.dynamoDb)
    client
  }

  private lazy val pressedTable = Table[FrontPressRecord](config.faciatool.frontPressUpdateTable)

  def getLastModified (path: String) = APIAuthAction { request =>
    import com.gu.scanamo.syntax._

    val record: Option[FrontPressRecord] = Scanamo.exec(client)(
        pressedTable.get('stageName -> "live" and 'frontId -> path)).flatMap(_.toOption)
    record.map(r => Ok(r.pressedTime)).getOrElse(NotFound)
  }

  def getLastModifiedStatus (stage: String, path: String) = APIAuthAction { request =>
    import com.gu.scanamo.syntax._

    val record: Option[FrontPressRecord] = Scanamo.exec(client)(
      pressedTable.get('stageName -> stage and 'frontId -> path)).flatMap(_.toOption)
    record.map(r => Ok(Json.toJson(r))).getOrElse(NotFound)
  }
}
