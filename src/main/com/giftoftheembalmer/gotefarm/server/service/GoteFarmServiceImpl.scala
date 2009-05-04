package com.giftoftheembalmer.gotefarm.server.service

import org.springframework.transaction.annotation.Transactional

import com.giftoftheembalmer.gotefarm.server.dao.GoteFarmDaoT

import com.giftoftheembalmer.gotefarm.client.{
  JSCharacter,
  JSEventSchedule,
  JSEventSignups,
  JSEventTemplate
}

import com.google.appengine.api.users.User

import org.apache.commons.logging.LogFactory

import java.util.{
  Calendar,
  Date,
  GregorianCalendar,
  TimeZone
}

import scala.collection.jcl.Conversions._

@Transactional{val readOnly = true}
class GoteFarmServiceImpl extends GoteFarmServiceT {
  private val logger = LogFactory.getLog(this.getClass)

  @scala.reflect.BeanProperty
  private var goteFarmDao: GoteFarmDaoT = null

  @Transactional{val readOnly = false}
  def generateTables() = goteFarmDao.generateTables()

  def login(username: String, password: String) =
    goteFarmDao.validateUser(username, password)

  @Transactional{val readOnly = false}
  def newUser(username: String, email: String, password: String) =
    goteFarmDao.createUser(username, email, password)
  @Transactional{val readOnly = false}
  def newCharacter(user: User, realm: String, character: String) =
    goteFarmDao.createCharacter(user, realm, character)

  def getCharacters(uid: Long) = goteFarmDao.getCharacters(uid)
  def getCharacter(cid: Long) = goteFarmDao.getCharacter(cid)

  def getRoles = goteFarmDao.getRoles
  @Transactional{val readOnly = false}
  def addRole(name: String, restricted: Boolean) =
    goteFarmDao.addRole(name, restricted)

  @Transactional{val readOnly = false}
  def updateCharacterRole(uid: Long, cid: Long, roleid: Long,
                          adding: Boolean): Unit = {
    // character must belong to user
    val chr = goteFarmDao.getCharacter(cid)
    if (chr.accountid != uid) {
      throw new IllegalArgumentException("Character does not belong to you.")
    }
    goteFarmDao.updateCharacterRole(cid, roleid, adding)
  }

  def getBadges = goteFarmDao.getBadges
  @Transactional{val readOnly = false}
  def addBadge(name: String, score: Int) =
    goteFarmDao.addBadge(name, score)

  @Transactional{val readOnly = false}
  def updateCharacterBadge(uid: Long, cid: Long, badgeid: Long,
                           adding: Boolean): Unit = {
    // character must belong to user
    val chr = goteFarmDao.getCharacter(cid)
    if (chr.accountid != uid) {
      throw new IllegalArgumentException("Character does not belong to you.")
    }
    goteFarmDao.updateCharacterBadge(cid, badgeid, adding)
  }

  @Transactional{val readOnly = false}
  def addInstance(name: String) = goteFarmDao.addInstance(name)
  @Transactional{val readOnly = false}
  def addBoss(instance: String, boss: String) =
    goteFarmDao.addBoss(instance, boss)

  def getInstances() = goteFarmDao.getInstances
  def getInstanceBosses(instance: String) =
    goteFarmDao.getInstanceBosses(instance)

  def getEventTemplate(name: String) = goteFarmDao.getEventTemplate(name)
  def getEventTemplates = goteFarmDao.getEventTemplates
  @Transactional{val readOnly = false}
  def saveEventTemplate(et: JSEventTemplate) =
    goteFarmDao.saveEventTemplate(et)

  def getEventSchedules(name: String) =
    goteFarmDao.getEventSchedules(name)
  @Transactional{val readOnly = false}
  def saveEventSchedule(es: JSEventSchedule) = {
    if (es.repeat_size < JSEventSchedule.REPEAT_NEVER || es.repeat_size > JSEventSchedule.REPEAT_MONTHLY) {
      throw new IllegalArgumentException("Illegal repeat size")
    }
    if (es.repeat_size > JSEventSchedule.REPEAT_NEVER && es.repeat_freq < 1) {
      throw new IllegalArgumentException("Illegal repeat freq")
    }
    if (es.repeat_size == JSEventSchedule.REPEAT_WEEKLY && (es.day_mask & 0x7F) == 0) {
      throw new IllegalArgumentException("Illegal day mask")
    }
    if (   es.repeat_size == JSEventSchedule.REPEAT_MONTHLY
        && (   es.repeat_by < JSEventSchedule.REPEAT_BY_DAY_OF_MONTH
            || es.repeat_by > JSEventSchedule.REPEAT_BY_DAY_OF_WEEK)) {
      throw new IllegalArgumentException("Illegal repeat by")
    }

    val r = goteFarmDao.saveEventSchedule(es)

    // publish events right away
    publishEvents()

    r
  }

  private def getEventNextOccurrence(event: JSEventSchedule): Date = {
    val h = event.timezone_offset / 60 * -1
    // FIXME: This ignores daylight saving time
    val m = Math.abs(event.timezone_offset % 60)
    val tz = String.format("GMT%+03d%02d", Array(int2Integer(h), int2Integer(m)): _*)
    val cal = new GregorianCalendar(TimeZone.getTimeZone(tz))
    cal.setFirstDayOfWeek(Calendar.SUNDAY)
    cal.setTime(event.start_time)

    event.repeat_size match {
      case JSEventSchedule.REPEAT_NEVER =>
        throw new IllegalStateException("Event does not repeat")

      case JSEventSchedule.REPEAT_DAILY =>
        cal.add(Calendar.DAY_OF_MONTH, event.repeat_freq)
        cal.getTime

      case JSEventSchedule.REPEAT_WEEKLY =>
        // look for the next day this week, or skip ahead repeat_freq weeks
        if (event.day_mask == 0) {
          throw new IllegalStateException("Event repeats weekly but no days are set")
        }

        def scanForNextDay(cal: Calendar, allow_sunday: Boolean): Option[Date] = {
          val dom = cal.get(Calendar.DAY_OF_WEEK)

          if (!allow_sunday && dom == Calendar.SUNDAY) {
            // rolled forward into next week, no match
            None
          }
          else if ((event.day_mask & (1<<(dom-1))) != 0) {
            // this day's bit is set, a match
            Some(cal.getTime)
          }
          else {
            // try the next day
            cal.add(Calendar.DAY_OF_MONTH, 1)
            scanForNextDay(cal, false)
          }
        }

        // pre-seek one day forward
        cal.add(Calendar.DAY_OF_MONTH, 1)

        scanForNextDay(cal, false).getOrElse({
          // no match for the current week, so seek forward to the next
          // Sunday, skipping repeat_freq weeks
          val start_of_week = new GregorianCalendar(TimeZone.getTimeZone(tz))
          start_of_week.setTime(event.start_time)
          start_of_week.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
          start_of_week.add(Calendar.WEEK_OF_YEAR, event.repeat_freq)
          scanForNextDay(start_of_week, true).getOrElse({
            throw new RuntimeException("Unexpected None in scanForNextDay")
          })
        })

      case JSEventSchedule.REPEAT_MONTHLY =>
        val orig_cal = new GregorianCalendar(TimeZone.getTimeZone(tz))
        orig_cal.setFirstDayOfWeek(Calendar.SUNDAY)
        orig_cal.setTime(event.orig_start_time)

        event.repeat_by match {
          case JSEventSchedule.REPEAT_BY_DAY_OF_MONTH =>
            val dom = orig_cal.get(Calendar.DAY_OF_MONTH)
            cal.add(Calendar.MONTH, event.repeat_freq)
            val max_dom = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            cal.set(Calendar.DAY_OF_MONTH, if (dom > max_dom) max_dom else dom)
            cal.getTime

          case JSEventSchedule.REPEAT_BY_DAY_OF_WEEK =>
            val dow = orig_cal.get(Calendar.DAY_OF_WEEK)
            val dowim = orig_cal.get(Calendar.DAY_OF_WEEK_IN_MONTH)
            cal.add(Calendar.MONTH, event.repeat_freq)
            // go to middle of month...
            cal.set(Calendar.DAY_OF_MONTH, 15)
            // so changing the day won't change the month
            cal.set(Calendar.DAY_OF_WEEK, dow)
            val month = cal.get(Calendar.MONTH)
            // changing the DAY_OF_WEEK_IN_MONTH might change the month
            cal.set(Calendar.DAY_OF_WEEK_IN_MONTH, dowim)
            while (cal.get(Calendar.MONTH) != month) {
              // so subtract weeks to get back to the same month
              cal.add(Calendar.WEEK_OF_YEAR, -1)
            }
            cal.getTime
        }

      case _ => // invalid value
        throw new IllegalStateException("Unknown repeat_size value");
    }
  }

  def getEvents = goteFarmDao.getEvents
  def getEventSignups(eventid: Long, if_changed_since: Date)
    : Option[JSEventSignups] =
    goteFarmDao.getEventSignups(eventid, if_changed_since)

  private def validateSignup(uid: Long, eventid: Long, chr: JSCharacter,
                             roleid: Long) = {
    // character must belong to user
    if (chr.accountid != uid) {
      throw new IllegalArgumentException("Signup does not belong to you.")
    }

    // character must have roleid
    if (!chr.hasRole(roleid)) {
      throw new IllegalArgumentException("Character missing role.")
    }

    // signups must not be closed
    val event = goteFarmDao.getEvent(eventid)
    val now = System.currentTimeMillis
    if (now > event.signups_end.getTime) {
      throw new IllegalArgumentException("Signups are closed for this event.")
    }

    // character must be able to signup
    val signups_start = event.signups_start.getTime
    if (now < signups_start) {
      // character needs to qualify for an early signup
      lazy val role = goteFarmDao.getRole(roleid)
      if (!event.badges.exists({ badge =>
           ((now >= signups_start - badge.earlySignup * 3600000L)
        && ((badge.applyToRole eq null) || badge.applyToRole == role.name)
        && chr.hasBadge(badge.badgeid))
      })) {
        throw new IllegalArgumentException("Character cannot sign up for this event yet.")
      }
    }
  }

  private def validateSignup(uid: Long, eventid: Long, cid: Long,
                             roleid: Long): Unit = {
    val chr = goteFarmDao.getCharacter(cid)
    validateSignup(uid, eventid, chr, roleid)
  }

  @Transactional{val readOnly = false}
  def signupForEvent(uid: Long, eventid: Long, cid: Long, roleid: Long,
                     signup_type: Int) = {
    validateSignup(uid, eventid, cid, roleid)
    goteFarmDao.signupForEvent(eventid, cid, roleid, signup_type)
  }

  @Transactional{val readOnly = false}
  def changeEventSignup(uid: Long, eventsignupid: Long, new_roleid: Long,
                        new_signup_type: Int): Unit = {
    val es = goteFarmDao.getEventSignup(eventsignupid)
    validateSignup(uid, es.eventid, es.chr, new_roleid)
    goteFarmDao.changeEventSignup(eventsignupid, new_roleid, new_signup_type)
  }

  @Transactional{val readOnly = false}
  def removeEventSignup(uid: Long, eventsignupid: Long): Unit = {
    // character must belong to user
    val es = goteFarmDao.getEventSignup(eventsignupid)
    if (es.chr.accountid != uid) {
      throw new IllegalArgumentException("Signup does not belong to you.")
    }
    goteFarmDao.removeEventSignup(eventsignupid)
  }

  @Transactional{val readOnly = false}
  def publishEvents() = synchronized {
    val reftime = System.currentTimeMillis

    def keepPublishing(event: JSEventSchedule): Unit = {
      // publish as soon as the display time is hit
      if (event.start_time.getTime - event.display_start * 1000 <= reftime) {
        logger.debug("publishing " + event.esid)
        logger.debug(event.start_time)
        logger.debug("repeat_size: " + event.repeat_size)
        logger.debug("repeat_freq: " + event.repeat_freq)
        logger.debug("day_mask: " + event.day_mask)
        logger.debug("repeat_by: " + event.repeat_by)

        goteFarmDao.publishEvent(event)

        // update to next time
        if (event.repeat_size != JSEventSchedule.REPEAT_NEVER) {
          event.start_time = getEventNextOccurrence(event)
          logger.debug("Next: " + event.start_time)

          // save new start time
          goteFarmDao.saveEventSchedule(event)

          // try to publish again
          keepPublishing(event)
        }
        else {
          // one-shot event, deactive it
          event.active = false
          goteFarmDao.saveEventSchedule(event)
        }
      }
    }

    for (event <- goteFarmDao.getActiveEventSchedules) {
      keepPublishing(event)
    }
  }
}
