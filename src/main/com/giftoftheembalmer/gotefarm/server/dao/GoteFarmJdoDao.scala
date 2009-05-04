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

import org.apache.commons.logging.LogFactory;

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

  def getRegions: java.util.Collection[Region] = {
    find(classOf[Region])
  }

  def getChrClass(key: Key): Option[ChrClass] = {
    getObjectById(classOf[ChrClass], key)
  }

  def getInstance(key: Key): Option[Instance] = {
    getObjectById(classOf[Instance], key)
  }

  def getRace(key: Key): Option[Race] = {
    getObjectById(classOf[Race], key)
  }

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

  def getRegion(key: Key): Option[Region] = {
    getObjectById(classOf[Region], key)
  }

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

  def getRealm(key: Key): Option[Realm] = {
    getObjectById(classOf[Realm], key)
  }

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
      val nr = new Guild(region, realm, name, account)
      getJdoTemplate.makePersistent(nr)
      nr
    }
    else {
      r.iterator.next
    }
  }

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

  def getCharacters(user: User, guild: Key) = {
    find(classOf[Chr], "user == userParam && guild == guildParam ",
           "com.google.appengine.api.users.User userParam, "
         + "com.google.appengine.api.datastore.Key guildParam")(user, guild)
  }

  /*
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

  def getRoles = {
    val jdbc = getSimpleJdbcTemplate()

    jdbc.query(
      "select " + JSRoleMapper.columns + " from role order by name",
      JSRoleMapper,
      Array[AnyRef](): _*
    )
  }

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

  def addRole(name: String, restricted: Boolean) = {
    val jdbc = getSimpleJdbcTemplate()
    try {
      jdbc.update(
        """insert into role (name, restricted)
                     values (?,    ?         )""",
        Array[AnyRef](name, boolchar(restricted)): _*
      )
    }
    catch {
      case _: DataIntegrityViolationException =>
        throw new AlreadyExistsError("Role '" + name + "' already exists.")
    }

    jdbc.queryForLong(
      "select roleid from role where name = ?",
      Array[AnyRef](name): _*
    )
  }

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

  def getBadges = {
    val jdbc = getSimpleJdbcTemplate()

    jdbc.query(
      "select " + JSBadgeMapper.columns + " from badge order by score desc, name",
      JSBadgeMapper
    )
  }

  def addBadge(name: String, score: Int) = {
    val jdbc = getSimpleJdbcTemplate()
    try {
      jdbc.update(
        """insert into badge (name, score)
                      values (?,    ?    )""",
        Array[AnyRef](name, score): _*
      )
    }
    catch {
      case _: DataIntegrityViolationException =>
        throw new AlreadyExistsError("Badge '" + name + "' already exists.")
    }

    jdbc.queryForLong(
      "select badgeid from badge where name = ?",
      Array[AnyRef](name): _*
    )
  }

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

  def getEventTemplate(name: String) = {
    val jdbc = getSimpleJdbcTemplate()

    val jset = try {
      jdbc.queryForObject(
        "select " + JSEventTemplateMapper.columns
                  + " from "
                  + JSEventTemplateMapper.tables
                  + " where eventtmpl.name = ?",
        JSEventTemplateMapper,
        Array[AnyRef](name): _*
      )
    }
    catch {
      case _: IncorrectResultSizeDataAccessException =>
        throw new NotFoundError("Template '" + name + "' not found.")
    }

    populateEventTemplate(jset)
  }

  def getEventTemplates = {
    val jdbc = getSimpleJdbcTemplate()
    val jsets = jdbc.query(
      "select " + JSEventTemplateMapper.columns
                + " from "
                + JSEventTemplateMapper.tables
                + " order by eventtmpl.name",
      JSEventTemplateMapper,
      Array[AnyRef](): _*
    ).map(populateEventTemplate)

    val r = new scala.collection.jcl.ArrayList[JSEventTemplate]
    r.addAll(jsets)
    r
  }

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

  def saveEventTemplate(et: JSEventTemplate) = {
    val jdbc = getSimpleJdbcTemplate()

    val iid = getInstanceId(et.instance)

    val new_event = et.eid == -1

    if (new_event) {
      try {
        jdbc.update(
          """insert into eventtmpl (name, size, minimum_level, instanceid)
                            values (?,    ?,    ?,             ?         )""",
          Array[AnyRef](et.name, et.size, et.minimumLevel, iid): _*
        )

        et.eid = jdbc.queryForLong(
          "select eventtmplid from eventtmpl where name = ?",
          Array[AnyRef](et.name): _*
        )
      }
      catch {
        case _: DataIntegrityViolationException =>
          throw new AlreadyExistsError("Template '" + et.name + "' already exists.")
      }
    }
    else {
      try {
        val c = jdbc.update(
          """update eventtmpl set name = ?, size = ?, minimum_level = ?, instanceid = ? where eventtmplid = ?""",
          Array[AnyRef](et.name, et.size, et.minimumLevel, iid, et.eid): _*
        )

        if (c == 0) {
          throw new NotFoundError("Template " + et.eid + " not found.")
        }
      }
      catch {
        case _: DataIntegrityViolationException =>
          throw new AlreadyExistsError("Template '" + et.name + "' already exists.")
      }
    }

    jdbc.update(
      """delete from eventtmplboss where eventtmplid = ?""",
      Array[AnyRef](et.eid): _*
    )

    for (boss <- et.bosses) {
      val bossid = try {
        jdbc.queryForLong(
          "select bossid from boss where instanceid = ? and name = ?",
          Array[AnyRef](iid, boss): _*
        )
      }
      catch {
        case _: IncorrectResultSizeDataAccessException =>
          throw new NotFoundError("Boss '" + boss + "' not found.")
      }

      jdbc.update(
        """insert into eventtmplboss (eventtmplid, bossid)
                              VALUES (?,           ?     )""",
        Array[AnyRef](et.eid, bossid): _*
      )
    }

    jdbc.update(
      """delete from eventtmplrole where eventtmplid = ?""",
      Array[AnyRef](et.eid): _*
    )

    for (role <- et.roles) {
      val roleid = try {
        jdbc.queryForLong(
          "select roleid from role where name = ?",
          Array[AnyRef](role.name): _*
        )
      }
      catch {
        case _: IncorrectResultSizeDataAccessException =>
          throw new NotFoundError("Role '" + role.name + "' not found.")
      }

      try {
        jdbc.update(
          """insert into eventtmplrole (eventtmplid, roleid, min_count, max_count)
                                VALUES (?,           ?,      ?,         ?        )""",
          Array[AnyRef](et.eid, roleid, role.min, role.max): _*
        )
      }
      catch {
        case e: DataIntegrityViolationException
          if e.getMessage.contains("MIN_COUNT_GE_ZERO") =>
            throw new IllegalArgumentException(
                "Role '"
              + role.name
              + "' must have a non-negative min value.")

        case e: DataIntegrityViolationException
          if e.getMessage.contains("MAX_COUNT_GT_ZERO") =>
            throw new IllegalArgumentException(
                "Role '"
              + role.name
              + "' must have a max value greater than zero.")
      }
    }

    jdbc.update(
      """delete from eventtmplbadge where eventtmplid = ?""",
      Array[AnyRef](et.eid): _*
    )

    // XXX: JCL wrapper creating weird compiler errors for et.badges
    val iter = et.badges.iterator
    while (iter.hasNext) {
      val badge = iter.next

      val badgeid = try {
        jdbc.queryForLong(
          "select badgeid from badge where name = ?",
          Array[AnyRef](badge.name): _*
        )
      }
      catch {
        case _: IncorrectResultSizeDataAccessException =>
          throw new NotFoundError("Badge '" + badge.name + "' not found.")
      }

      val roleid: Option[java.lang.Long] = if (badge.applyToRole ne null) {
        try {
          Some(jdbc.queryForLong(
            "select roleid from role where name = ?",
            Array[AnyRef](badge.applyToRole): _*
          ))
        }
        catch {
          case _: IncorrectResultSizeDataAccessException =>
            throw new NotFoundError("Role '" + badge.applyToRole + "' not found.")
        }
      }
      else {
        None
      }

      jdbc.update(
        """insert into eventtmplbadge (eventtmplid, badgeid, require_for_signup, roleid, num_slots, early_signup)
                               VALUES (?,           ?,       ?,                  ?,      ?,         ?           )""",
        Array[AnyRef](et.eid, badgeid, boolchar(badge.requireForSignup), roleid.getOrElse(null), badge.numSlots, badge.earlySignup): _*
      )

      ()
    }

    if (!new_event && et.modifyEvents) {
      rebuildEvents(et);
    }

    et.eid
  }

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

  def getEventSchedules(name: String) = {
    val jdbc = getSimpleJdbcTemplate()

    jdbc.query(
      "select " + JSEventScheduleMapper.columns +
      """ from eventsched join eventtmpl
            on eventsched.eventtmplid = eventtmpl.eventtmplid
          where eventtmpl.name = ?
          order by start_time""",
      JSEventScheduleMapper,
      Array[AnyRef](name): _*
    )
  }

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
