package com.giftoftheembalmer.gotefarm.server.servlet

import com.giftoftheembalmer.gotefarm.server.service._

import org.springframework.web.servlet.mvc.AbstractController

import javax.servlet.http.{
  HttpServlet,
  HttpServletRequest,
  HttpServletResponse
}

class LoginServlet extends AbstractController {
  @scala.reflect.BeanProperty
  private var goteFarmService: GoteFarmServiceT = null

  private def sessionID(req: HttpServletRequest, uid: Long) = {
    val sess = req.getSession()
    if (sess eq null) {
      throw new RuntimeException("session is null")
    }
    sess.putValue("uid", uid)
    logger.debug("Returning new sessionID: " + sess.getId())
    sess.getId()
  }

  override def handleRequestInternal(req: HttpServletRequest,
                                     resp: HttpServletResponse) = {
    val username = req.getParameter("username")
    val password = req.getParameter("password")

    try {
      val uid = goteFarmService.login(username, password)

      resp.getWriter.print("OK," + sessionID(req, uid) + ",")
    }
    catch {
      case _ =>
        resp.getWriter.print("NOK")
    }

    resp.flushBuffer()

    null
  }
}
