package com.giftoftheembalmer.gotefarm.server.servlet

import org.springframework.web.servlet.FrameworkServlet

import javax.servlet.http.{
  HttpServletRequest,
  HttpServletResponse
}

import com.giftoftheembalmer.gotefarm.server.service._

class Character(val name: String)
{
  val traits = new scala.collection.mutable.HashMap[String,Int]
}

class Account(val name: String)
{
  val chars = new scala.collection.mutable.HashMap[String,Character]
}

class Event(val name: String, val count: Int)
{
  val requirements = new scala.collection.mutable.ListBuffer[String]
}

class Main extends FrameworkServlet
{
  private var goteFarmService: GoteFarmServiceT = null

  override def initFrameworkServlet() = {
    goteFarmService = getWebApplicationContext().getBean("goteFarmService").asInstanceOf[GoteFarmServiceT]
  }

  val accounts = new scala.collection.mutable.HashMap[String,Account]

  accounts.put("Ivry", {
    val a = new Account("Ivry")
    a.chars.put("Ivry", {
      val c = new Character("Ivry")
      c.traits.put("healer", 1)
      c.traits.put("shackle", 1)
      c.traits.put("healing_power", 2200)
      c
    })
    a.chars.put("Meteshield", {
      val c = new Character("Meteshield")
      c.traits.put("tank", 1)
      c
    })
    a
  })

  val event = {
    val e = new Event("Karazhan", 10)
    e.requirements += "num(tank) == 2"
    e.requirements += "num(shackle) > 1"
    e.requirements += "num(healer) > 1"
    e.requirements += "sum(healing_power) >= 2000"
    e.requirements += "num(melee_dps) <= 2"
    e.requirements += "num(aoe) > 1"
    e
  }

  val signups = "Ivry" -> "Meteshield" :: "Ivry" -> "Ivry" :: Nil

  def solveRaid(event: Event, queue: List[(String,String)]): Option[(List[Character],List[Character])] = {
    import scala.collection.immutable.{Map,Set,HashMap,HashSet}

    def mergeState(state: Map[String,Int], traits: scala.collection.Map[String,Int]): Map[String,Int] = {
      val combkeys = state.keySet ++ traits.keySet
      HashMap.empty ++ (for (k <- combkeys) yield k -> (state.getOrElse(k, 0) + traits.getOrElse(k, 0)))
    }

    def solveRaid(
      event: Event,
      openings: Int,
      roster: List[Character],
      accounts_in_roster: Set[String],
      state: Map[String,Int],
      standby: List[Character],
      queue: List[(String, String)]): Option[(List[Character],List[Character])] = {

      for {
        (acct, char) <- queue.firstOption
        if !accounts_in_roster.contains(acct)
        acct <- accounts.get(acct)
        char <- acct.chars.get(char)
        new_state = mergeState(state, char.traits)
      } yield {
      }

      null
    }

    solveRaid(event, event.count, Nil, HashSet.empty, HashMap.empty, Nil, queue)
  }

  override def doService(request: HttpServletRequest, response: HttpServletResponse) = {
    response.setContentType("text/html")
    val out = response.getWriter()
    out.println("<html>Hello World</html>")

    // goteFarmService.test()
    val r = solveRaid(event, signups)
    println(r)
  }
}
