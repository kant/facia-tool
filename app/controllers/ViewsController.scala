package controllers

import auth.PanDomainAuthActions
import com.gu.pandomainauth.action.UserRequest
import conf.ApplicationConfiguration
import model.Cached
import permissions.ConfigPermissionCheck
import play.api.libs.ws.WSClient
import play.api.mvc._
import services.AssetsManager
import util.{Acl, Encryption}

class ViewsController(val config: ApplicationConfiguration, val acl: Acl, assetsManager: AssetsManager, isDev: Boolean,
                      crypto: Encryption, override val wsClient: WSClient) extends Controller with PanDomainAuthActions {

  def priorities() = AuthAction { request =>
    val identity = request.user
    Cached(60) {
      Ok(views.html.priority(Option(identity), config.facia.stage, isDev))
    }
  }

  def collectionEditor() = AuthAction { request =>
    val identity = request.user
    Cached(60) {
      Ok(views.html.admin_main(Option(identity), config.facia.stage, overrideIsDev(request, isDev),
        assetsManager.pathForCollections, crypto.encrypt(identity.email)))
    }
  }

  def configEditor() = (AuthAction andThen new ConfigPermissionCheck(acl)) { request =>
    val identity = request.user
    Cached(60) {
      Ok(views.html.admin_main(Option(identity), config.facia.stage, overrideIsDev(request, isDev),
        assetsManager.pathForConfig, crypto.encrypt(identity.email)))
    }
  }

  private def overrideIsDev(request: UserRequest[AnyContent], isDev: Boolean): Boolean = {
    request.queryString.getOrElse("isDev", Seq(if (isDev) "true" else "false")).contains("true")
  }
}
