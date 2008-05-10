package com.giftoftheembalmer.gotefarm.servlet

import org.springframework.web.servlet.FrameworkServlet

import javax.servlet.http.{
  HttpServletRequest,
  HttpServletResponse
}

class Main extends FrameworkServlet
{
  override def doService(request: HttpServletRequest, response: HttpServletResponse) = {
    response.setContentType("text/html")
    val out = response.getWriter()
    out.println("<html>Hello World</html>")
  }
}
