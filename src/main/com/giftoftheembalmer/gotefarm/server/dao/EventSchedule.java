package com.giftoftheembalmer.gotefarm.server.dao;

import com.google.appengine.api.datastore.Key;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import java.util.Date;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class EventSchedule {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;

    @Persistent
    private Key guild;

    @Persistent
    private Key eventTemplate;

    @Persistent
    private Date startTime;

    @Persistent
    private Date origStartTime;

    @Persistent
    private String timeZone;

    @Persistent
    private int duration;

    @Persistent
    private int displayStart;

    @Persistent
    private int displayEnd;

    @Persistent
    private int signupsStart;

    @Persistent
    private int signupsEnd;

    @Persistent
    private int repeatSize;

    @Persistent
    private int repeatFreq;

    @Persistent
    private int dayMask;

    @Persistent
    private int repeatBy;

    @Persistent
    private boolean active;

    public EventSchedule(Key guild, Key eventTemplate, Date startTime,
                         String timeZone, int duration, int displayStart,
                         int displayEnd, int signupsStart, int signupsEnd,
                         int repeatSize, int repeatFreq, int dayMask,
                         int repeatBy, boolean active) {
        this.guild = guild;
        this.eventTemplate = eventTemplate;
        this.startTime = startTime;
        this.origStartTime = startTime;
        this.timeZone = timeZone;
        this.duration = duration;
        this.displayStart = displayStart;
        this.displayEnd = displayEnd;
        this.signupsStart = signupsStart;
        this.signupsEnd = signupsEnd;
        this.repeatSize = repeatSize;
        this.repeatFreq = repeatFreq;
        this.dayMask = dayMask;
        this.repeatBy = repeatBy;
        this.active = active;
    }

    public int getDayMask() {
        return dayMask;
    }

    public int getDisplayEnd() {
        return displayEnd;
    }

    public int getDisplayStart() {
        return displayStart;
    }

    public int getDuration() {
        return duration;
    }

    public Key getEventTemplate() {
        return eventTemplate;
    }

    public Key getGuild() {
        return guild;
    }

    public Key getKey() {
        return key;
    }

    public Date getOrigStartTime() {
        return origStartTime;
    }

    public int getRepeatBy() {
        return repeatBy;
    }

    public int getRepeatFreq() {
        return repeatFreq;
    }

    public int getRepeatSize() {
        return repeatSize;
    }

    public int getSignupsEnd() {
        return signupsEnd;
    }

    public int getSignupsStart() {
        return signupsStart;
    }

    public Date getStartTime() {
        return startTime;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setDayMask(int dayMask) {
        this.dayMask = dayMask;
    }

    public void setDisplayEnd(int displayEnd) {
        this.displayEnd = displayEnd;
    }

    public void setDisplayStart(int displayStart) {
        this.displayStart = displayStart;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setOrigStartTime(Date origStartTime) {
        this.origStartTime = origStartTime;
    }

    public void setRepeatBy(int repeatBy) {
        this.repeatBy = repeatBy;
    }

    public void setRepeatFreq(int repeatFreq) {
        this.repeatFreq = repeatFreq;
    }

    public void setRepeatSize(int repeatSize) {
        this.repeatSize = repeatSize;
    }

    public void setSignupsEnd(int signupsEnd) {
        this.signupsEnd = signupsEnd;
    }

    public void setSignupsStart(int signupsStart) {
        this.signupsStart = signupsStart;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }
}
