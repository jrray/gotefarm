package com.giftoftheembalmer.gotefarm.servlet

import org.springframework.web.servlet.FrameworkServlet

import javax.servlet.http.{
  HttpServletRequest,
  HttpServletResponse
}

import com.giftoftheembalmer.gotefarm.service._

class Main extends FrameworkServlet
{
  private var goteFarmService: GoteFarmServiceT = null

  override def initFrameworkServlet() = {
    goteFarmService = getWebApplicationContext().getBean("goteFarmService").asInstanceOf[GoteFarmServiceT]
  }

  override def doService(request: HttpServletRequest, response: HttpServletResponse) = {
    response.setContentType("text/html")
    val out = response.getWriter()
    out.println("<html>Hello World</html>")

    // goteFarmService.test()
  }
}
