package com.giftoftheembalmer.gotefarm.server.servlet

import com.giftoftheembalmer.gotefarm.server.service._

import com.giftoftheembalmer.gotefarm.client.{
  GoteFarmRPC,
  JSAccount,
  JSBadge,
  JSBoss,
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

  override
  def getGuildFromArmoryURL(url: String): JSGuild = {
    goteFarmService.getGuildFromArmoryURL(getUser, url)
  }

  override
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

  override
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

  override
  def getRegions: java.util.List[JSRegion] = goteFarmService.getRegions

  private def getUser: User = {
    val user = userService.getCurrentUser
    if (user eq null) throw new UserNotLoggedInError
    user
  }

  override
  def newCharacter(guild_key: String, character: String) = {
    goteFarmService.newCharacter(getUser, guild_key, character)
  }

  override
  def getCharacters(guild_key: String) = {
    goteFarmService.getCharacters(getUser, guild_key)
  }

  override
  def getCharacter(sid: String, cid: Long) = {
    /*
    val sess = getSession(sid)
    goteFarmService.getCharacter(cid)
    */
    new JSCharacter
  }

  override
  def getRoles(guild_key: String): java.util.List[JSRole] = {
    goteFarmService.getRoles(guild_key)
  }

  override
  def addRole(guild_key: String, name: String, restricted: Boolean): JSRole = {
    goteFarmService.addRole(getUser, guild_key, name, restricted)
  }

  override
  def updateCharacterRole(sid: String, cid: Long, role_key: String,
                          adding: Boolean): JSCharacter = {
    /*
    val sess = getSession(sid)
    val uid = sess.getValue("uid").asInstanceOf[Long]
    goteFarmService.updateCharacterRole(uid, cid, roleid, adding)
    goteFarmService.getCharacter(cid)
    */
    new JSCharacter
  }

  override
  def getBadges(guild_key: String): java.util.List[JSBadge] = {
    goteFarmService.getBadges(guild_key)
  }

  override
  def addBadge(guild_key: String, name: String, score: Int): JSBadge = {
    goteFarmService.addBadge(getUser, guild_key, name, score)
  }

  override
  def updateCharacterBadge(sid: String, cid: Long, badge_key: String,
                           adding: Boolean): JSCharacter = {
    /*
    val sess = getSession(sid)
    val uid = sess.getValue("uid").asInstanceOf[Long]
    goteFarmService.updateCharacterBadge(uid, cid, badgeid, adding)
    goteFarmService.getCharacter(cid)
    */
    new JSCharacter
  }

  override
  def addInstance(guild_key: String, name: String): JSInstance = {
    goteFarmService.addInstance(getUser, guild_key, name)
  }

  override
  def addBoss(instance_key: String, name: String): JSBoss = {
    goteFarmService.addBoss(getUser, instance_key, name)
  }

  override
  def getInstances(guild_key: String): java.util.List[JSInstance] = {
    goteFarmService.getInstances(guild_key)
  }

  override
  def getInstanceBosses(instance_key: String): java.util.List[JSBoss] = {
    goteFarmService.getInstanceBosses(instance_key)
  }

  override
  def getEventTemplate(sid: String, name: String) = {
    /*
    val sess = getSession(sid)
    goteFarmService.getEventTemplate(name)
    */
    new JSEventTemplate
  }

  override
  def getEventTemplates(guild_key: String) = {
    goteFarmService.getEventTemplates(getUser, guild_key)
  }

  override
  def saveEventTemplate(guild_key: String, et: JSEventTemplate)
    : JSEventTemplate = {
    goteFarmService.saveEventTemplate(getUser, guild_key, et)
  }

  override
  def getEventSchedules(sid: String, name: String) = {
    /*
    val sess = getSession(sid)
    goteFarmService.getEventSchedules(name)
    */
    new java.util.ArrayList[JSEventSchedule]
  }

  override
  def saveEventSchedule(sid: String, es: JSEventSchedule) = {
    /*
    val sess = getSession(sid)
    goteFarmService.saveEventSchedule(es)
    true
    */
    false
  }

  override
  def getEvents(sid: String) = {
    /*
    val sess = getSession(sid)
    goteFarmService.getEvents
    */
    new java.util.ArrayList[JSEvent]
  }

  override
  def getEventSignups(sid: String, event_key: String,
                      if_changed_since: Date) = {
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

  override
  def signupForEvent(sid: String, event_key: String, cid: Long,
                     role_key: String, signup_type: Int) = {
    /*
    val sess = getSession(sid)
    val uid = sess.getValue("uid").asInstanceOf[Long]
    goteFarmService.signupForEvent(uid, eventid, cid, roleid, signup_type)
    forceGetEvents(eventid)
    */
    null
  }

  override
  def changeEventSignup(sid: String, event_key: String, eventsignupid: Long,
                        new_role_key: String, new_signup_type: Int) = {
    /*
    val sess = getSession(sid)
    val uid = sess.getValue("uid").asInstanceOf[Long]
    goteFarmService.changeEventSignup(uid, eventsignupid, new_roleid,
                                      new_signup_type)
    forceGetEvents(eventid)
    */
    null
  }

  override
  def removeEventSignup(sid: String, event_key: String,
                        eventsignupid: Long) = {
    /*
    val sess = getSession(sid)
    val uid = sess.getValue("uid").asInstanceOf[Long]
    goteFarmService.removeEventSignup(uid, eventsignupid)
    forceGetEvents(eventid)
    */
    null
  }
}
