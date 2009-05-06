package com.giftoftheembalmer.gotefarm.server.dao

import com.giftoftheembalmer.gotefarm.client.{
  AlreadyExistsError,
  InvalidCredentialsError,
  JSBadge,
  JSCharacter,
  JSChrBadge,
  JSChrRole,
  JSEvent,
  JSEventBadge,
  JSEventRole,
  JSEventSchedule,
  JSEventSignup,
  JSEventSignups,
  JSEventTemplate,
  JSRole,
  NotFoundError
}

import com.google.appengine.api.datastore.Key
import com.google.appengine.api.users.User

import java.util.Date

import scala.collection.jcl.Conversions._

import scala.Predef.{
  intWrapper => _,
  longWrapper => _,
  _
}

class GoteFarmJdoDao extends ScalaJdoDaoSupport
  with GoteFarmDaoT {

  override
  def getAccount(user: User): Account = {
    val r = find(classOf[Account], "user == userParam",
                 "com.google.appengine.api.users.User userParam")(user)
    if (r.isEmpty) {
      // add it
      val nr = new Account(user)
      getJdoTemplate.makePersistent(nr)
      nr
    }
    else {
      val a = r.iterator.next
      a.setLastUpdate(new Date)
      a
    }
  }

  /*
  override
  def validateUser(username: String, password: String) = {
    val jdbc = getSimpleJdbcTemplate()
    val acct = jdbc.query(
      "select accountid, password from account where username = ?",
      new ParameterizedRowMapper[(Long, String)] {
        def mapRow(rs: ResultSet, rowNum: Int) = {
          (rs.getLong(1), rs.getString(2))
        }
      },
      Array[AnyRef](username): _*
    )

    if (acct.isEmpty) {
      throw new InvalidCredentialsError
    }

    // validate password
    if (!BCrypt.checkpw(password, acct(0)._2)) {
      throw new InvalidCredentialsError
    }

    acct(0)._1
  }

  override
  def createUser(username: String, email: String, password: String) = {
    val crypt = BCrypt.hashpw(password, BCrypt.gensalt(12));

    val jdbc = getSimpleJdbcTemplate()

    try {
      jdbc.update(
        """insert into account (username, email, password, admin, created)
                        values (?,        ?,     ?,        ?,     CURRENT_TIMESTAMP)""",
        Array[AnyRef](username, email, crypt, "N"): _*
      )
    }
    catch {
      case _: DataIntegrityViolationException =>
        throw new AlreadyExistsError("User '" + username + "' already exists.")

      case e =>
        logger.debug("Error creating user", e)
        throw new RuntimeException("Error creating user: " + e.getMessage)
    }

    jdbc.queryForLong(
      "select accountid from account where username = ?",
      Array[AnyRef](username): _*
    )
  }
  */

  override
  def getBoss(key: Key): Option[Boss] = {
    getObjectById(classOf[Boss], key)
  }

  override
  def getRegions: java.util.Collection[Region] = {
    find(classOf[Region])
  }

  override
  def getChrClass(key: Key): Option[ChrClass] = {
    getObjectById(classOf[ChrClass], key)
  }

  override
  def getInstance(key: Key): Option[Instance] = {
    getObjectById(classOf[Instance], key)
  }

  override
  def getRace(key: Key): Option[Race] = {
    getObjectById(classOf[Race], key)
  }

  override
  def getRace(race: String): Race = {
    val r = find(classOf[Race], "name == nameParam",
                 "java.lang.String nameParam")(race)
    if (r.isEmpty) {
      // add it
      val nr = new Race(race)
      getJdoTemplate.makePersistent(nr)
      nr
    }
    else {
      r.iterator.next
    }
  }

  override
  def getRegion(key: Key): Option[Region] = {
    getObjectById(classOf[Region], key)
  }

  override
  def getRegion(code: String): Region = {
    val r = find(classOf[Region], "code == codeParam",
                 "java.lang.String codeParam")(code)
    if (r.isEmpty) {
      // add it
      val nr = new Region(code)
      getJdoTemplate.makePersistent(nr)
      nr
    }
    else {
      r.iterator.next
    }
  }

  override
  def getRealm(key: Key): Option[Realm] = {
    getObjectById(classOf[Realm], key)
  }

  override
  def getRealm(region: String, name: String): Realm = {
    val r = find(
      classOf[Realm], "region == regionParam && name == nameParam",
      "java.lang.String regionParam, java.lang.String nameParam"
    )(region, name)
    if (r.isEmpty) {
      // add it
      val nr = new Realm(region, name, null)
      getJdoTemplate.makePersistent(nr)
      nr
    }
    else {
      r.iterator.next
    }
  }

  override
  def getGuild(key: Key): Option[Guild] = {
    getObjectById(classOf[Guild], key)
  }

  override
  def getGuild(region: String, realm: String, name: String, account: Key)
    : Guild = {
    val r = find(
      classOf[Guild],
      "region == regionParam && realm == realmParam && name == nameParam",
        "java.lang.String regionParam, java.lang.String realmParam,"
      + "java.lang.String nameParam"
    )(region, realm, name)
    if (r.isEmpty) {
      // add it
      val nr = new Guild(region, realm, name, account, null)
      getJdoTemplate.makePersistent(nr)
      nr
    }
    else {
      r.iterator.next
    }
  }

  override
  def getChrClass(clazz: String): ChrClass = {
    val r = find(classOf[ChrClass], "name == nameParam",
                 "java.lang.String nameParam")(clazz)
    if (r.isEmpty) {
      // add it
      val nr = new ChrClass(clazz)
      getJdoTemplate.makePersistent(nr)
      nr
    }
    else {
      r.iterator.next
    }
  }

  override
  def getCharacter(guild: Key, name: String): Option[Chr] = {
    val r = find(classOf[Chr], "guild == guildParam && name == nameParam",
                   "com.google.appengine.api.datastore.Key guildParam, "
                 + "java.lang.String nameParam")(guild, name)
    if (r.isEmpty) {
      None
    }
    else {
      Some(r.iterator.next)
    }
  }

  override
  def createCharacter(user: User, guild: Key, character: String, race: Race,
                      clazz: ChrClass, level: Int, chrxml: String) = {
    val jdo = getJdoTemplate
    val c = new Chr(user, guild, character, race.getKey, clazz.getKey, level,
                    chrxml.toString, new Date)
    jdo.makePersistent(c)
    c
  }

  /*
  def getCharacterRoles(cid: Long): Seq[JSChrRole] = {
    val jdbc = getSimpleJdbcTemplate()
    jdbc.query(
      "select " + JSChrRoleMapper.columns + " from role join chrrole on role.roleid = chrrole.roleid where chrid = ? order by name",
      JSChrRoleMapper,
      Array[AnyRef](cid): _*
    )
  }

  def getCharacterBadges(cid: Long): Seq[JSChrBadge] = {
    val jdbc = getSimpleJdbcTemplate()
    jdbc.query(
      "select " + JSChrBadgeMapper.columns + " from badge join chrbadge on badge.badgeid = chrbadge.badgeid where chrid = ? order by name",
      JSChrBadgeMapper,
      Array[AnyRef](cid): _*
    )
  }
  */

  override
  def getCharacters(user: User, guild: Key) = {
    find(classOf[Chr], "user == userParam && guild == guildParam ",
           "com.google.appengine.api.users.User userParam, "
         + "com.google.appengine.api.datastore.Key guildParam")(user, guild)
  }

  /*
  override
  def getCharacter(cid: Long) = {
    val jdbc = getSimpleJdbcTemplate()
    try {
      val chr = jdbc.queryForObject(
        "select " + JSCharacterMapper.columns + " from "
          + JSCharacterMapper.tables
          + """ where chrid = ?""",
        JSCharacterMapper,
        Array[AnyRef](cid): _*
      )

      chr.roles = getCharacterRoles(chr.cid).toArray
      chr.badges = getCharacterBadges(chr.cid).toArray

      chr
    }
    catch {
      case _: IncorrectResultSizeDataAccessException =>
        throw new NotFoundError("Character not found.")
    }
  }
  */

  override
  def getRole(key: Key): Option[Role] = {
    getObjectById(classOf[Role], key)
  }

  override
  def getRoles(guild: Key): java.util.Collection[Role] = {
    find(classOf[Role], "guild == guildParam",
         "com.google.appengine.api.datastore.Key guildParam")(guild)
  }

  /*
  override
  def getRole(roleid: Long) = {
    val jdbc = getSimpleJdbcTemplate()

    try {
      jdbc.queryForObject(
        "select " + JSRoleMapper.columns + " from role where roleid = ?",
        JSRoleMapper,
        roleid: AnyRef
      )
    }
    catch {
      case _: IncorrectResultSizeDataAccessException =>
        throw new NotFoundError("Role not found.")
    }
  }
  */

  override
  def addRole(guild: Key, name: String, restricted: Boolean): Role = {
    val nr = new Role(guild, name, restricted)
    getJdoTemplate.makePersistent(nr)
    nr
  }

  /*
  override
  def updateCharacterRole(cid: Long, roleid: Long, adding: Boolean): Unit = {
    val jdbc = getSimpleJdbcTemplate()
    if (adding) {
      try {
        jdbc.update("""insert into chrrole (chrid, roleid, waiting, approved)
                        VALUES (?, ?, 'Y', 'N')""",
                    cid, roleid)
      }
      catch {
        case e: DataIntegrityViolationException if e.getMessage.contains("CHRROLE_CHRID_FK") =>
          throw new NotFoundError

        case e: DataIntegrityViolationException if e.getMessage.contains("UNIQUE") =>
          // role already there, ignore
      }
    }
    else {
      // TODO: Update last_signup_modification for any event that this
      // character is signed up for with the role being deleted.

      // TODO: Delete any signups by this character with this role.

      jdbc.update("""delete from chrrole where chrid = ? and roleid = ?""",
                  cid, roleid)
    }
  }
  */

  override
  def getBadge(key: Key): Option[Badge] = {
    getObjectById(classOf[Badge], key)
  }

  override
  def getBadges(guild: Key): java.util.Collection[Badge] = {
    find(classOf[Badge], "guild == guildParam",
         "com.google.appengine.api.datastore.Key guildParam")(guild)
  }

  override
  def addBadge(guild: Key, name: String, score: Int): Badge = {
    val nb = new Badge(guild, name, score)
    getJdoTemplate.makePersistent(nb)
    nb
  }

  /*
  override
  def updateCharacterBadge(cid: Long, badgeid: Long, adding: Boolean): Unit = {
    val jdbc = getSimpleJdbcTemplate()
    if (adding) {
      try {
        jdbc.update("""insert into chrbadge (chrid, badgeid, waiting, approved)
                        VALUES (?, ?, 'Y', 'N')""",
                    cid, badgeid)
      }
      catch {
        case e: DataIntegrityViolationException if e.getMessage.contains("CHRBADGE_CHRID_FK") =>
          throw new NotFoundError

        case e: DataIntegrityViolationException if e.getMessage.contains("UNIQUE") =>
          // badge already there, ignore
      }
    }
    else {
      // TODO: Update last_signup_modification for any event that this
      // character is signed up for that has badge requirements involving
      // the badge being deleted.

      jdbc.update("""delete from chrbadge where chrid = ? and badgeid = ?""",
                  cid, badgeid)
    }
  }
  */

  override
  def addInstance(guild: Key, name: String): Instance = {
    val ni = new Instance(guild, name)
    getJdoTemplate.makePersistent(ni)
    ni
  }

  override
  def getInstances(guild: Key): java.util.Collection[Instance] = {
    find(classOf[Instance], "guild == guildParam",
         "com.google.appengine.api.datastore.Key guildParam")(guild)
  }

  override
  def addEventTemplate(guild: Key, name: String, size: Int, min_level: Int,
                       instance: String, instance_key: Key): EventTemplate = {
    val ne = new EventTemplate(guild, name, size, min_level, instance,
                               instance_key)
    getJdoTemplate.makePersistent(ne)
    ne
  }

  /*
  private def populateEventTemplate[T <: JSEventTemplate](et: T,
                                                          eventstub: String)
    :T = {

    val jdbc = getSimpleJdbcTemplate()
    val ops = jdbc.getJdbcOperations()

    ops.query(
      """select boss.name
          from boss join """ + eventstub + """boss
            on boss.bossid = """ + eventstub + """boss.bossid
          where """ + eventstub + """id = ?""",
      Array[AnyRef](et.eid),
      new RowCallbackHandler {
        def processRow(rs: ResultSet) = {
          et.bosses.add(rs.getString(1))
        }
      }
    )

    ops.query(
      """select role.roleid, role.name, min_count, max_count
          from role join """ + eventstub + """role
            on role.roleid = """ + eventstub + """role.roleid
          where """ + eventstub + """id = ?""",
      Array[AnyRef](et.eid),
      new RowCallbackHandler {
        def processRow(rs: ResultSet) = {
          val ev = new JSEventRole

          ev.roleid = rs.getLong(1)
          ev.name = rs.getString(2)
          ev.min = rs.getInt(3)
          ev.max = rs.getInt(4)

          et.roles.add(ev)
        }
      }
    )

    ops.query(
      """select badge.badgeid, badge.name, require_for_signup, role.name, num_slots, early_signup
          from
            """ + eventstub + """badge left outer join role
              on """ + eventstub + """badge.roleid = role.roleid,
            badge
          where
            """ + eventstub + """badge.badgeid = badge.badgeid
            and """ + eventstub + """id = ?""",
      Array[AnyRef](et.eid),
      new RowCallbackHandler {
        def processRow(rs: ResultSet) = {
          val eb = new JSEventBadge

          eb.badgeid = rs.getLong(1)
          eb.name = rs.getString(2)
          eb.requireForSignup = charbool(rs.getString(3))
          eb.applyToRole = rs.getString(4)
          eb.numSlots = rs.getInt(5)
          eb.earlySignup = rs.getInt(6)

          et.badges.add(eb)
        }
      }
    )

    et
  }

  private def populateEventTemplate(et: JSEventTemplate): JSEventTemplate =
    populateEventTemplate(et, "eventtmpl")

  private def populateEvent(e: JSEvent): JSEvent =
    populateEventTemplate(e, "event")
  */

  override
  def getEventTemplate(key: Key): Option[EventTemplate] = {
    getObjectById(classOf[EventTemplate], key)
  }

  override
  def getEventTemplates(guild: Key): java.util.Collection[EventTemplate] = {
    find(classOf[EventTemplate], "guild == guildParam",
         "com.google.appengine.api.datastore.Key guildParam")(guild)
  }

  /*
  private def rebuildEvents(et: JSEventTemplate): Unit = {
    val jdbc = getSimpleJdbcTemplate

    val iid = getInstanceId(et.instance)

    for {
      leventid <- jdbc.query("""select eventid from event
                                  where event.eventtmplid = ? for update""",
                             new ParameterizedRowMapper[java.lang.Long] {
                               def mapRow(rs: ResultSet, rowNum: Int) = {
                                 rs.getLong(1)
                               }
                             },
                             et.eid)
      eventid = leventid.longValue
    } {
      // update the event row itself
      jdbc.update("""update event set
        name = ?,
        size = ?,
        minimum_level = ?,
        instanceid = ?
        where eventid = ?""",
        et.name, et.size, et.minimumLevel, iid, eventid)

      // delete and recreate the bosses, roles and badges
      jdbc.update("delete from eventrole where eventid = ?", eventid)
      populateEventRoles(eventid, et.eid)

      jdbc.update("delete from eventbadge where eventid = ?", eventid)
      populateEventBadges(eventid, et.eid)

      jdbc.update("delete from eventboss where eventid = ?", eventid)
      populateEventBosses(eventid, et.eid)
    }
  }

  override
  def getActiveEventSchedules = {
    val jdbc = getSimpleJdbcTemplate()

    jdbc.query(
      "select " + JSEventScheduleMapper.columns +
      """ from eventsched join eventtmpl
            on eventsched.eventtmplid = eventtmpl.eventtmplid
          where active = 'Y'""",
      JSEventScheduleMapper,
      noargs: _*
    )
  }
  */

  override
  def getEventSchedule(key: Key): Option[EventSchedule] = {
    getObjectById(classOf[EventSchedule], key)
  }

  override
  def getEventSchedules(event_template: Key)
    : java.util.Collection[EventSchedule] = {
    find(classOf[EventSchedule], "eventTemplate == etParam",
         "com.google.appengine.api.datastore.Key etParam")(event_template)
  }

  override
  def getGuildEventSchedules(guild: Key)
    : java.util.Collection[EventSchedule] = {
    find(classOf[EventSchedule], "guild == guildParam",
         "com.google.appengine.api.datastore.Key guildParam")(guild)
  }

  /*
  override
  def saveEventSchedule(es: JSEventSchedule) = {
    val jdbc = getSimpleJdbcTemplate

    if (es.esid == -1) {
      // new schedule
      jdbc.update(
        """insert into eventsched
            (eventtmplid, active,
             start_time, orig_start_time,
             timezone_offset, duration,
             display_start, display_end,
             signups_start, signups_end,
             repeat_size, repeat_freq,
             day_mask, repeat_by)
            VALUES
            (?, ?,
             ?, ?,
             ?, ?,
             ?, ?,
             ?, ?,
             ?, ?,
             ?, ?)""",
          Array[AnyRef](es.eid, boolchar(es.active),
                        es.start_time, es.orig_start_time,
                        es.timezone_offset, es.duration,
                        es.display_start, es.display_end,
                        es.signups_start, es.signups_end,
                        es.repeat_size, es.repeat_freq,
                        es.day_mask, es.repeat_by): _*)

      jdbc.queryForLong("values IDENTITY_VAL_LOCAL()", noargs: _*)
    }
    else {
      // update existing
      val r = jdbc.update(
        """update eventsched
            set
              eventtmplid = ?, active = ?,
              start_time = ?, orig_start_time = ?,
              timezone_offset = ?, duration = ?,
              display_start = ?, display_end = ?,
              signups_start = ?, signups_end = ?,
              repeat_size = ?, repeat_freq = ?, day_mask = ?,
              repeat_by = ?
            where
              eventschedid = ?""",
          Array[AnyRef](es.eid, boolchar(es.active),
                        es.start_time, es.orig_start_time,
                        es.timezone_offset, es.duration,
                        es.display_start, es.display_end,
                        es.signups_start, es.signups_end,
                        es.repeat_size, es.repeat_freq, es.day_mask,
                        es.repeat_by, es.esid): _*)

      if (r == 0) {
        throw new NotFoundError(  "Event schedule not found, "
                                + "did somebody else delete it?")
      }

      es.esid
    }
  }

  private def populateEventRoles(eventid: Long, eventtmplid: Long): Unit = {
    val jdbc = getSimpleJdbcTemplate
    jdbc.update(
      """insert into eventrole (eventid, roleid, min_count, max_count)
          select ?, roleid, min_count, max_count
            from eventtmplrole
            where eventtmplid = ?""",
      Array[AnyRef](eventid, eventtmplid): _*)
  }

  private def populateEventBadges(eventid: Long, eventtmplid: Long): Unit = {
    val jdbc = getSimpleJdbcTemplate
    jdbc.update(
      """insert into eventbadge (eventid, badgeid, require_for_signup, roleid,
          num_slots, early_signup)
          select ?, badgeid, require_for_signup, roleid, num_slots,
            early_signup
            from eventtmplbadge
            where eventtmplid = ?""",
      Array[AnyRef](eventid, eventtmplid): _*)
  }

  private def populateEventBosses(eventid: Long, eventtmplid: Long): Unit = {
    val jdbc = getSimpleJdbcTemplate
    jdbc.update(
      """insert into eventboss (eventid, bossid)
          select ?, bossid
            from eventtmplboss
            where eventtmplid = ?""",
      Array[AnyRef](eventid, eventtmplid): _*)
  }

  override
  def publishEvent(es: JSEventSchedule) = {
    val jdbc = getSimpleJdbcTemplate

    // create event

    //   calculate display/signup dates
    val display_start = new Date(es.start_time.getTime - es.display_start * 1000)
    val display_end = new Date(es.start_time.getTime + es.display_end * 1000)
    val signups_start = new Date(es.start_time.getTime - es.signups_start * 1000)
    val signups_end = new Date(es.start_time.getTime + es.signups_end * 1000)

    val jset = jdbc.queryForObject(
      "select " + JSEventTemplateMapper.columns
                + " from "
                + JSEventTemplateMapper.tables
                + " where eventtmpl.eventtmplid = ?",
      JSEventTemplateMapper,
      Array[AnyRef](es.eid): _*
    )

    val iid = getInstanceId(jset.instance)

    jdbc.update(
      """insert into event (eventtmplid, name, size, minimum_level, instanceid,
          start_time, duration, display_start, display_end, signups_start,
          signups_end)
          VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
      Array[AnyRef](es.eid, jset.name, jset.size, jset.minimumLevel, iid,
        es.start_time, es.duration, display_start, display_end,
        signups_start, signups_end
      ): _*)

    val eventid = jdbc.queryForLong("values IDENTITY_VAL_LOCAL()", noargs: _*)

    populateEventRoles(eventid, es.eid)
    populateEventBadges(eventid, es.eid)
    populateEventBosses(eventid, es.eid)
  }

  override
  def getEvents = {
    val jdbc = getSimpleJdbcTemplate

    val events = jdbc.query(
      "select " + JSEventMapper.columns
                + " from "
                + JSEventMapper.tables
                + """ where event.display_start <= current_timestamp
                        and event.display_end >= current_timestamp
                      order by event.start_time""",
      JSEventMapper,
      noargs: _*
    )

    for (event <- events) {
      populateEvent(event)
    }

    events
  }

  override
  def getEvent(eventid: Long) = {
    val jdbc = getSimpleJdbcTemplate

    val event = try {
      jdbc.queryForObject(
        "select " + JSEventMapper.columns
                  + " from "
                  + JSEventMapper.tables
                  + " where eventid = ?",
        JSEventMapper,
        eventid: AnyRef
      )
    }
    catch {
      case _: IncorrectResultSizeDataAccessException =>
        throw new NotFoundError("No such event")
    }

    populateEvent(event)

    event
  }

  override
  def getEventSignups(eventid: Long,
                      if_changed_since: Date): Option[JSEventSignups] = {
    val jdbc = getSimpleJdbcTemplate
    val last_modification = try {
      jdbc.queryForObject(
        "select last_signup_modification from event where eventid = ?",
        classOf[java.sql.Timestamp],
        eventid
      )
    }
    catch {
      case _: IncorrectResultSizeDataAccessException =>
        throw new NotFoundError("No such event")
    }

    if (last_modification.getTime > if_changed_since.getTime) {
      val r = new JSEventSignups
      r.eventid = eventid
      r.signups = jdbc.query(
        JSEventSignupMapper.prefix +
          """ where eventid = ?
          order by signup_time""",
        JSEventSignupMapper,
        eventid
      )

      for (es <- r.signups) {
        es.chr.roles = getCharacterRoles(es.chr.cid).toArray
        es.chr.badges = getCharacterBadges(es.chr.cid).toArray
      }

      r.asof = last_modification

      Some(r)
    }
    else {
      None
    }
  }

  override
  def getEventSignup(eventsignupid: Long) = {
    val jdbc = getSimpleJdbcTemplate
    try {
      val r = jdbc.queryForObject(
        JSEventSignupMapper.prefix +
          " where eventsignupid = ?",
        JSEventSignupMapper,
        eventsignupid: AnyRef
      )

      r.chr.roles = getCharacterRoles(r.chr.cid).toArray
      r.chr.badges = getCharacterBadges(r.chr.cid).toArray

      r
    }
    catch {
      case _: IncorrectResultSizeDataAccessException =>
        throw new NotFoundError("No such event signup")
    }
  }

  override
  def signupForEvent(eventid: Long, cid: Long, roleid: Long,
                     signup_type: Int): Unit = {
    val jdbc = getSimpleJdbcTemplate
    try {
      jdbc.update(
        """insert into eventsignup
            (eventid, chrid, roleid, signup_type, signup_time)
            values (?, ?, ?, ?, current_timestamp)""",
        eventid, cid, roleid, signup_type
      )
    }
    catch {
      case e: DataIntegrityViolationException
        if e.getMessage.contains("UNIQUE") =>
          throw new AlreadyExistsError("Character already signed up")

      case e: DataIntegrityViolationException
        if e.getMessage.contains("EVENTID_FK") =>
          throw new NotFoundError("No such event")

      case e: DataIntegrityViolationException
        if e.getMessage.contains("CHRID_FK") =>
          throw new NotFoundError("No such character")

      case e: DataIntegrityViolationException
        if e.getMessage.contains("ROLEID_FK") =>
          throw new NotFoundError("No such role")
    }
  }

  override
  def changeEventSignup(eventsignupid: Long, new_roleid: Long,
                        new_signup_type: Int): Unit = {
    // changing roles does not change signup time,
    // but changing types does.
    val jdbc = getSimpleJdbcTemplate
    try {
      val c = jdbc.update(
        """update eventsignup
            set signup_time = case
                                when signup_type = ? then signup_time
                                else current_timestamp
                              END,
                roleid = ?,
                signup_type = ?
            where eventsignupid = ?""",
        new_signup_type, new_roleid, new_signup_type, eventsignupid
      )

      if (c == 0) {
        throw new NotFoundError("Signup not found")
      }
    }
    catch {
      case e: DataIntegrityViolationException
        if e.getMessage.contains("ROLEID_FK") =>
          throw new NotFoundError("No such role")
    }
  }

  override
  def removeEventSignup(eventsignupid: Long): Unit = {
    val jdbc = getSimpleJdbcTemplate
    val c = jdbc.update(
      """delete from eventsignup
          where eventsignupid = ?""",
      eventsignupid
    )

    if (c == 0) {
      throw new NotFoundError("Signup not found")
    }
  }
  */
}
