package com.giftoftheembalmer.gotefarm.dao

import org.springframework.jdbc.core._
import org.springframework.jdbc.core.simple._

import java.sql.PreparedStatement

class GoteFarmJdbcDao extends SimpleJdbcDaoSupport
  with GoteFarmDaoT {

  private def tableExists(name: String): Boolean = {
    try {
      getSimpleJdbcTemplate().queryForObject(
        """SELECT name FROM sqlite_master WHERE type='table' and name = ?""",
        classOf[java.lang.String],
        Array[AnyRef](name)
      )
      true
    }
    catch {
      case _: org.springframework.dao.IncorrectResultSizeDataAccessException =>
        false
    }
  }

  def generateTables() = {
    val jdbc = getSimpleJdbcTemplate().getJdbcOperations()

    jdbc.execute(
      """CREATE TABLE IF NOT EXISTS account (
        accountid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        username TEXT UNIQUE NOT NULL,
        password TEXT NOT NULL,
        admin INTEGER NOT NULL,
        created INTEGER NOT NULL,
        lastevent INTEGER
      )"""
    )

    if (!tableExists("race")) {
      jdbc.execute(
        """CREATE TABLE race (
          raceid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
          name TEXT UNIQUE NOT NULL
        )"""
      )

      val races = Array(
        "Draenei",
        "Dwarf",
        "Gnome",
        "Human",
        "Night Elf",
        "Blood Elf",
        "Orc",
        "Tauren",
        "Troll",
        "Undead"
      )

      jdbc.batchUpdate(
        """INSERT INTO race (name) VALUES (?)""",
        new BatchPreparedStatementSetter {
          def getBatchSize(): Int = races.size
          def setValues(ps: PreparedStatement, i: Int) {
            ps.setString(1, races(i))
          }
        }
      )
    }

    if (!tableExists("class")) {
      jdbc.execute(
        """CREATE TABLE class (
          classid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
          name TEXT UNIQUE NOT NULL
        )"""
      )

      val classes = Array(
        "Druid",
        "Hunter",
        "Mage",
        "Paladin",
        "Priest",
        "Rogue",
        "Shaman",
        "Warlock",
        "Warrior"
      )

      jdbc.batchUpdate(
        """INSERT INTO class (name) VALUES (?)""",
        new BatchPreparedStatementSetter {
          def getBatchSize(): Int = classes.size
          def setValues(ps: PreparedStatement, i: Int) {
            ps.setString(1, classes(i))
          }
        }
      )
    }

    jdbc.execute(
      """CREATE TABLE IF NOT EXISTS character (
        characterid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        accountid INTEGER NOT NULL,
        name TEXT UNIQUE NOT NULL,
        raceid INTEGER NOT NULL,
        classid INTEGER NOT NULL,
        characterxml TEXT,
        created INTEGER NOT NULL
      )"""
    )

    if (!tableExists("role")) {
      // A restricted role cannot be self-assigned to a character.
      // An admin can add restricted roles to any character.
      jdbc.execute(
        """CREATE TABLE role (
          roleid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
          name TEXT UNIQUE NOT NULL,
          restricted INTEGER NOT NULL
        )"""
      )

      val roles = Array(
        "Tank" -> 0,
        "Healer" -> 0,
        "DPS" -> 0,
        "AoE DPS" -> 0,
        "Mage Tank (Krosh)" -> 1,
        "Warlock Tank (Leotheras)" -> 1,
        "Nature Resist Tank (Hydross)" -> 1,
        "Frost Resist Tank (Hydross)" -> 1,
        "Nature/Frost Resist Tank (Hydross adds)" -> 1
      )

      jdbc.batchUpdate(
        """INSERT INTO role (name,restricted) VALUES (?,?)""",
        new BatchPreparedStatementSetter {
          def getBatchSize(): Int = roles.size
          def setValues(ps: PreparedStatement, i: Int) {
            ps.setString(1, roles(i)._1)
            ps.setInt(2, roles(i)._2)
          }
        }
      )
    }

    jdbc.execute(
      """CREATE TABLE IF NOT EXISTS chrrole (
        chrroleid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        characterid INTEGER NOT NULL,
        roleid INTEGER NOT NULL
      )"""
    )

    if (!tableExists("badge")) {
      // A badge is like a role, it is an attribute that a character can earn.
      // The score per badge is an arbitrary, unitless value used to compare
      // the relative worth of badges.
      jdbc.execute(
        """CREATE TABLE badge (
          badgeid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
          name TEXT UNIQUE NOT NULL,
          score INTEGER NOT NULL
        )"""
      )

      val badges = Array(
        "Karazhan Geared" -> 300,
        "Gruul/Mag Geared" -> 400,
        "ZA Geared" -> 450,
        "SSC/TK Geared" -> 500,
        "BT/HYJ Geared" -> 600,
        "Sunwell Geared" -> 700
      )

      jdbc.batchUpdate(
        """INSERT INTO badge (name,score) VALUES (?,?)""",
        new BatchPreparedStatementSetter {
          def getBatchSize(): Int = badges.size
          def setValues(ps: PreparedStatement, i: Int) {
            ps.setString(1, badges(i)._1)
            ps.setInt(2, badges(i)._2)
          }
        }
      )
    }

    jdbc.execute(
      """CREATE TABLE IF NOT EXISTS chrbadge (
        chrbadgeid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        characterid INTEGER NOT NULL,
        badgeid INTEGER NOT NULL
      )"""
    )

    jdbc.execute(
      """CREATE TABLE IF NOT EXISTS eventtmpl (
        eventtmplid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        name TEXT UNIQUE NOT NULL
      )"""
    )

    // event-global badge requirements
    // if present, a character must have earned the specified badge to sign
    // up for the event
    jdbc.execute(
      """CREATE TABLE IF NOT EXISTS eventtmplbadge (
        eventtmplbadgeid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        badgeid INTEGER NOT NULL
      )"""
    )

    jdbc.execute(
      """CREATE TABLE IF NOT EXISTS eventtmplrole (
        eventtmplroleid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        roleid INTEGER NOT NULL,
        count INTEGER NOT NULL CHECK (count > 0)
      )"""
    )

    // role-specific badge requirements
    // if present, a character must have earned the specified badge to be
    // eligible to sign up for the associated role
    jdbc.execute(
      """CREATE TABLE IF NOT EXISTS eventtmplrolebadge (
        eventtmplrolebadgeid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        eventtmplroleid INTEGER NOT NULL,
        badgeid INTEGER NOT NULL
      )"""
    )
  }
}
