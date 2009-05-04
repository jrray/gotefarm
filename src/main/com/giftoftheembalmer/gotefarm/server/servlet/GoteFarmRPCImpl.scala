package com.giftoftheembalmer.gotefarm.server.servlet

import com.giftoftheembalmer.gotefarm.server.service._

import com.giftoftheembalmer.gotefarm.client.{
  GoteFarmRPC,
  JSAccount,
  JSBadge,
  JSCharacter,
  JSEvent,
  JSEventSchedule,
  JSEventSignup,
  JSEventSignups,
  JSEventTemplate,
  JSInstance,
  JSGuild,
  JSRegion,
  JSRole,
  UserNotLoggedInError
}

import com.google.appengine.api.datastore.Key
import com.google.appengine.api.users.User
import com.google.appengine.api.users.UserServiceFactory

import com.google.gwt.user.server.rpc.RemoteServiceServlet

import org.gwtwidgets.server.spring.ServletUtils

import org.apache.commons.logging.LogFactory;

import java.util.Date

class GoteFarmRPCImpl extends RemoteServiceServlet
  with GoteFarmRPC {
  import GoteFarmServiceImpl._

  private val logger = LogFactory.getLog(this.getClass)
  logger.debug("Servlet running")

  @scala.reflect.BeanProperty
  private var goteFarmService: GoteFarmServiceT = null

  private val userService = UserServiceFactory.getUserService

  private def sessionID(uid: Long) = {
    val req = ServletUtils.getRequest()
    if (req eq null) {
      throw new RuntimeException("request is null")
    }
    val sess = req.getSession()
    if (sess eq null) {
      throw new RuntimeException("session is null")
    }
    sess.putValue("uid", uid)
    logger.debug("Returning new sessionID: " + sess.getId())
    sess.getId()
  }

  override
  def getAccount: JSAccount = {
    goteFarmService.getAccount(getUser)
  }

  def getGuildFromArmoryURL(url: String): JSGuild = {
    goteFarmService.getGuildFromArmoryURL(getUser, url)
  }

  def newUser(username: String, email: String, password: String) = {
    /*
    val uid = goteFarmService.newUser(username, email, password)
    sessionID(uid)
    */
    ""
  }

  private def getSession(sid: String) = {
    val req = ServletUtils.getRequest()
    val sess = req.getSession(false)
    if ((sess eq null) || sess.getId() != sid) {
      throw new UserNotLoggedInError
    }

    sess
  }

  def validateSID(sid: String) = {
    try {
      val sess = getSession(sid)
      sess.getId()
    }
    catch {
      case _: UserNotLoggedInError =>
        null
    }
  }

  def getRegions: java.util.List[JSRegion] = goteFarmService.getRegions

  private def getUser: User = {
    val user = userService.getCurrentUser
    if (user eq null) throw new UserNotLoggedInError
    user
  }

  def newCharacter(guild_key: String, character: String) = {
    goteFarmService.newCharacter(getUser, guild_key, character)
  }

  def getCharacters(guild_key: String) = {
    goteFarmService.getCharacters(getUser, guild_key)
  }

  def getCharacter(sid: String, cid: Long) = {
    /*
    val sess = getSession(sid)
    goteFarmService.getCharacter(cid)
    */
    new JSCharacter
  }

  def getRoles =
    new java.util.ArrayList[JSRole] // goteFarmService.getRoles

  def addRole(sid: String, name: String, restricted: Boolean) = {
    /*
    val sess = getSession(sid)
    goteFarmService.addRole(name, restricted)
    true
    */
    false
  }

  def updateCharacterRole(sid: String, cid: Long, roleid: Long, adding: Boolean) = {
    /*
    val sess = getSession(sid)
    val uid = sess.getValue("uid").asInstanceOf[Long]
    goteFarmService.updateCharacterRole(uid, cid, roleid, adding)
    goteFarmService.getCharacter(cid)
    */
    new JSCharacter
  }

  def getBadges =
    new java.util.ArrayList[JSBadge] // goteFarmService.getBadges

  def addBadge(sid: String, name: String, score: Int) = {
    /*
    val sess = getSession(sid)
    goteFarmService.addBadge(name, score)
    true
    */
    false
  }

  def updateCharacterBadge(sid: String, cid: Long, badgeid: Long, adding: Boolean) = {
    /*
    val sess = getSession(sid)
    val uid = sess.getValue("uid").asInstanceOf[Long]
    goteFarmService.updateCharacterBadge(uid, cid, badgeid, adding)
    goteFarmService.getCharacter(cid)
    */
    new JSCharacter
  }

  def addInstance(guild_key: String, name: String): JSInstance = {
    goteFarmService.addInstance(getUser, guild_key, name)
  }

  def addBoss(sid: String, instance: String, boss: String) = {
    /*
    val sess = getSession(sid)
    goteFarmService.addBoss(instance, boss)
    true
    */
    false
  }

  def getInstances(guild_key: String): java.util.List[JSInstance] = {
    goteFarmService.getInstances(guild_key)
  }

  def getInstanceBosses(instance: String) =
    new java.util.ArrayList[String] // goteFarmService.getInstanceBosses(instance)

  def getEventTemplate(sid: String, name: String) = {
    /*
    val sess = getSession(sid)
    goteFarmService.getEventTemplate(name)
    */
    new JSEventTemplate
  }

  def getEventTemplates(sid: String) = {
    /*
    val sess = getSession(sid)
    goteFarmService.getEventTemplates
    */
    new java.util.ArrayList[JSEventTemplate]
  }

  def saveEventTemplate(sid: String, et: JSEventTemplate) = {
    /*
    val sess = getSession(sid)
    goteFarmService.saveEventTemplate(et)
    true
    */
    false
  }

  def getEventSchedules(sid: String, name: String) = {
    /*
    val sess = getSession(sid)
    goteFarmService.getEventSchedules(name)
    */
    new java.util.ArrayList[JSEventSchedule]
  }

  def saveEventSchedule(sid: String, es: JSEventSchedule) = {
    /*
    val sess = getSession(sid)
    goteFarmService.saveEventSchedule(es)
    true
    */
    false
  }

  def getEvents(sid: String) = {
    /*
    val sess = getSession(sid)
    goteFarmService.getEvents
    */
    new java.util.ArrayList[JSEvent]
  }

  def getEventSignups(sid: String, eventid: Long, if_changed_since: Date) = {
    /*
    val sess = getSession(sid)
    goteFarmService.getEventSignups(eventid, if_changed_since)
      .getOrElse(null)
    */
    null
  }

  private val time_zero = new Date(0L)
  private def forceGetEvents(eventid: Long) = {
    /*
    goteFarmService.getEventSignups(eventid, time_zero).getOrElse({
      val r = new JSEventSignups
      r.eventid = eventid
      r.signups = new java.util.ArrayList[JSEventSignup]
      r.asof = time_zero
      r
    })
    */
    null
  }

  def signupForEvent(sid: String, eventid: Long, cid: Long, roleid: Long,
                     signup_type: Int) = {
    /*
    val sess = getSession(sid)
    val uid = sess.getValue("uid").asInstanceOf[Long]
    goteFarmService.signupForEvent(uid, eventid, cid, roleid, signup_type)
    forceGetEvents(eventid)
    */
    null
  }

  def changeEventSignup(sid: String, eventid: Long, eventsignupid: Long,
                        new_roleid: Long, new_signup_type: Int) = {
    /*
    val sess = getSession(sid)
    val uid = sess.getValue("uid").asInstanceOf[Long]
    goteFarmService.changeEventSignup(uid, eventsignupid, new_roleid,
                                      new_signup_type)
    forceGetEvents(eventid)
    */
    null
  }

  def removeEventSignup(sid: String, eventid: Long, eventsignupid: Long) = {
    /*
    val sess = getSession(sid)
    val uid = sess.getValue("uid").asInstanceOf[Long]
    goteFarmService.removeEventSignup(uid, eventsignupid)
    forceGetEvents(eventid)
    */
    null
  }
}
