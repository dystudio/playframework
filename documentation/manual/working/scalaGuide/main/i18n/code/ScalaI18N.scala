/*
 * Copyright (C) 2009-2016 Lightbend Inc. <https://www.lightbend.com>
 */
package scalaguide.i18n.scalai18n {
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api._
import play.api.http.HttpConfiguration
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc._
import play.api.test._

package views.html {
  object formpage {
    def apply()(implicit messages: play.api.i18n.Messages): String = {
      ""
    }
  }
}

//#i18n-support
import javax.inject.Inject
import play.api.i18n._

class MyController @Inject()(cc: ControllerComponents) extends AbstractController(cc) with I18nSupport {

  def index = Action { implicit request =>
    // type enrichment through I18nSupport
    val messages: Messages = request.messages
    val message: String = messages("info.error")
    Ok(message)
  }

  def messages2 = Action { implicit request =>
    // type enrichment through I18nSupport
    val lang: Lang = request.lang
    val message: String = messagesApi("info.error")(lang)
    Ok(message)
  }

  def messages3 = Action { request =>
    // direct access with no implicits required
    val messages: Messages = messagesApi.preferred(request)
    val lang = messages.lang
    val message: String = messages("info.error")
    Ok(message)
  }

  def messages4 = Action { implicit request =>
    // takes implicit Messages, converted using request2messages
    // template defined with @()(implicit messages: Messages)
    Ok(views.html.formpage())
  }
}
//#i18n-support

@RunWith(classOf[JUnitRunner])
class ScalaI18nSpec extends PlaySpecification with Controller {
  val conf = Configuration.reference ++ Configuration.from(Map("play.i18n.path" -> "scalaguide/i18n"))

  "A controller" should {

    "return the right message" in new WithApplication(GuiceApplicationBuilder().loadConfig(conf).build()) {
      val controller = app.injector.instanceOf[MyController]

      val result = controller.index(FakeRequest())
      contentAsString(result) must contain("You aren't logged in!")
    }
  }

  "A Scala translation" should {

    val langs = new DefaultLangsProvider(conf).get
    val httpConfiguration = HttpConfiguration.fromConfiguration(conf)
    val messagesApi = new DefaultMessagesApiProvider(Environment.simple(), conf, langs, httpConfiguration).get

    implicit val lang = Lang("en")

    "escape single quotes" in {
      //#apostrophe-messages
      messagesApi("info.error") == "You aren't logged in!"
      //#apostrophe-messages
    }

    "escape parameter substitution" in {
      //#parameter-escaping
      messagesApi("example.formatting") == "When using MessageFormat, '{0}' is replaced with the first parameter."
      //#parameter-escaping
    }
  }

}

}