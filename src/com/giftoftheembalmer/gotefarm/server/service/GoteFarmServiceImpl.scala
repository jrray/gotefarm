package com.giftoftheembalmer.gotefarm.server.service

import org.springframework.transaction.annotation.Transactional

import com.giftoftheembalmer.gotefarm.server.dao.GoteFarmDaoT

import com.giftoftheembalmer.gotefarm.client.{
  JSEventSchedule,
  JSEventTemplate
}

import org.apache.commons.logging.LogFactory

import java.util.{
  Calendar,
  Date,
  GregorianCalendar,
  TimeZone
}

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
  def newCharacter(uid: Long, realm: String, character: String) =
    goteFarmDao.createCharacter(uid, realm, character)

  def getCharacters(uid: Long) = goteFarmDao.getCharacters(uid)
  def getCharacter(cid: Long) = goteFarmDao.getCharacter(cid)

  def getRoles = goteFarmDao.getRoles
  @Transactional{val readOnly = false}
  def addRole(name: String, restricted: Boolean) =
    goteFarmDao.addRole(name, restricted)

  def getBadges = goteFarmDao.getBadges
  @Transactional{val readOnly = false}
  def addBadge(name: String, score: Int) =
    goteFarmDao.addBadge(name, score)

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
    if (es.repeat_size < 0 || es.repeat_size > 3) {
      throw new IllegalArgumentException("Illegal repeat size")
    }
    if (es.repeat_size > 0 && es.repeat_freq < 1) {
      throw new IllegalArgumentException("Illegal repeat freq")
    }
    if (es.repeat_size == 2 && (es.day_mask & 0x7F) == 0) {
      throw new IllegalArgumentException("Illegal day mask")
    }
    if (es.repeat_size == 3 && (es.repeat_by < 0 || es.repeat_by > 1)) {
      throw new IllegalArgumentException("Illegal repeat by")
    }

    goteFarmDao.saveEventSchedule(es)
  }

  private def getEventNextOccurrance(event: JSEventSchedule): Date = {
    val h = event.timezone_offset / 60 * -1
    // FIXME: This ignores daylight saving time
    val m = Math.abs(event.timezone_offset % 60)
    val tz = String.format("GMT%+03d%02d", Array(int2Integer(h), int2Integer(m)): _*)
    val cal = new GregorianCalendar(TimeZone.getTimeZone(tz))
    cal.setFirstDayOfWeek(Calendar.SUNDAY)
    cal.setTime(event.start_time)

    event.repeat_size match {
      case 0 => // repeat never
        throw new IllegalStateException("Event does not repeat")

      case 1 => // repeat daily
        cal.add(Calendar.DAY_OF_MONTH, event.repeat_freq)
        cal.getTime

      case 2 => // repeat weekly
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

      case 3 => // repeat monthly
        val orig_cal = new GregorianCalendar(TimeZone.getTimeZone(tz))
        orig_cal.setFirstDayOfWeek(Calendar.SUNDAY)
        orig_cal.setTime(event.orig_start_time)

        event.repeat_by match {
          case 0 => // day of the month
            val dom = orig_cal.get(Calendar.DAY_OF_MONTH)
            cal.add(Calendar.MONTH, event.repeat_freq)
            val max_dom = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            cal.set(Calendar.DAY_OF_MONTH, if (dom > max_dom) max_dom else dom)
            cal.getTime

          case 1 => // day of the week
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
}
