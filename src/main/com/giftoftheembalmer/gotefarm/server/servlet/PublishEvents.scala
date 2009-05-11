package com.giftoftheembalmer.gotefarm.server.servlet

import com.giftoftheembalmer.gotefarm.server.service.GoteFarmServiceT

import org.springframework.web.servlet.{
  ModelAndView,
  View
}
import org.springframework.web.servlet.mvc.AbstractController

import javax.servlet.http.{
  HttpServletRequest,
  HttpServletResponse
}

class PublishEvents extends AbstractController {
  @scala.reflect.BeanProperty
  private var goteFarmService: GoteFarmServiceT = _

  private val model_and_view = new ModelAndView(new PublishEventsView)

  class PublishEventsView extends View {
    override
    def getContentType: String = "text/text"

    override
    def render(model: java.util.Map[_,_], request: HttpServletRequest,
               response: HttpServletResponse): Unit = {
      val more = goteFarmService.publishEvent
      if (more) {
        response.sendRedirect(
          response.encodeRedirectURL(
            "publish_events?t=" + System.currentTimeMillis
          )
        )
      }
      else {
        val w = response.getWriter
        w.println("done")
      }
    }
  }

  override
  def handleRequestInternal(request: HttpServletRequest,
                            response: HttpServletResponse): ModelAndView = {
    model_and_view
  }
}
