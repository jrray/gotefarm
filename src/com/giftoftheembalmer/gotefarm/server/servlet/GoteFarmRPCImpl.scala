package com.giftoftheembalmer.gotefarm.server.servlet

import com.giftoftheembalmer.gotefarm.server.service._

import com.giftoftheembalmer.gotefarm.client.{
  GoteFarmRPC,
  JSEventTemplate,
  UserNotLoggedInError
}

import com.google.gwt.user.server.rpc.RemoteServiceServlet

import org.gwtwidgets.server.spring.ServletUtils

import org.apache.commons.logging.LogFactory;


class GoteFarmRPCImpl extends RemoteServiceServlet
  with GoteFarmRPC {

  private val logger = LogFactory.getLog(this.getClass)
  logger.debug("Servlet running")

  @scala.reflect.BeanProperty
  private var goteFarmService: GoteFarmServiceT = null

  private def sessionID(uid: Int) = {
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

  def login(username: String, password: String) = {
    val uid = goteFarmService.login(username, password)
    sessionID(uid)
  }

  def newUser(username: String, email: String, password: String) = {
    val uid = goteFarmService.newUser(username, email, password)
    sessionID(uid)
  }

  private def getSession(sid: String) = {
    val req = ServletUtils.getRequest()
    val sess = req.getSession(false)
    if (sess == null || sess.getId() != sid) {
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

  def newCharacter(sid: String, realm: String, character: String) = {
    val sess = getSession(sid)
    val uid = sess.getValue("uid").asInstanceOf[Int]
    goteFarmService.newCharacter(uid, realm, character)
  }

  def getCharacters(sid: String) = {
    val sess = getSession(sid)
    val uid = sess.getValue("uid").asInstanceOf[Int]
    goteFarmService.getCharacters(uid)
  }

  def getCharacter(sid: String, cid: Int) = {
    val sess = getSession(sid)
    goteFarmService.getCharacter(cid)
  }

  def getRoles =
    goteFarmService.getRoles

  def addRole(sid: String, name: String, restricted: Boolean) = {
    val sess = getSession(sid)
    goteFarmService.addRole(name, restricted)
    true
  }

  def getBadges =
    goteFarmService.getBadges

  def addBadge(sid: String, name: String, score: Int) = {
    val sess = getSession(sid)
    goteFarmService.addBadge(name, score)
    true
  }

  def addInstance(sid: String, name: String) = {
    val sess = getSession(sid)
    goteFarmService.addInstance(name)
    true
  }

  def addBoss(sid: String, instance: String, boss: String) = {
    val sess = getSession(sid)
    goteFarmService.addBoss(instance, boss)
    true
  }

  def getInstances = goteFarmService.getInstances

  def getInstanceBosses(instance: String) =
    goteFarmService.getInstanceBosses(instance)

  def getEventTemplate(sid: String, name: String) = {
    val sess = getSession(sid)
    goteFarmService.getEventTemplate(name)
  }

  def getEventTemplates(sid: String) = {
    val sess = getSession(sid)
    goteFarmService.getEventTemplates
  }

  def saveEventTemplate(sid: String, et: JSEventTemplate) = {
    val sess = getSession(sid)
    goteFarmService.saveEventTemplate(et)
    true
  }

  def getEventSchedules(sid: String, name: String) = {
    val sess = getSession(sid)
    goteFarmService.getEventSchedules(name)
  }
}
