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
      case _:org.springframework.dao.IncorrectResultSizeDataAccessException =>
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
  }
}
