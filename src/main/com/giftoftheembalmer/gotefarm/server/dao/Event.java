package com.giftoftheembalmer.gotefarm.server.dao;

import com.google.appengine.api.datastore.Key;

import javax.jdo.annotations.Element;
import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Order;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Event {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;

    @Persistent
    private Key eventTemplate;

    @Persistent
    private Key guild;

    @Persistent
    private String name;

    @Persistent
    private int size;

    @Persistent
    private int minimumLevel;

    @Persistent
    private String instance;

    @Persistent
    private Key instanceKey;

    // FIXME: Using List here due to AppEngine bug, switch to Set once
    // it is fixed.
    // http://code.google.com/p/datanucleus-appengine/issues/detail?id=26
    @Persistent
    @Element(dependent = "true")
    @Order(extensions = @Extension(vendorName = "datanucleus",
                                   key = "list-ordering",
                                   value = "boss asc"))
    private List<EventBoss> eventBosses = new ArrayList<EventBoss>();

    // FIXME: Using List here due to AppEngine bug, switch to Set once
    // it is fixed.
    // http://code.google.com/p/datanucleus-appengine/issues/detail?id=26
    @Persistent
    @Element(dependent = "true")
    @Order(extensions = @Extension(vendorName = "datanucleus",
                                   key = "list-ordering",
                                   value = "role asc"))
    private List<EventRole> eventRoles = new ArrayList<EventRole>();

    // FIXME: Using List here due to AppEngine bug, switch to Set once
    // it is fixed.
    // http://code.google.com/p/datanucleus-appengine/issues/detail?id=26
    @Persistent
    @Element(dependent = "true")
    @Order(extensions = @Extension(vendorName = "datanucleus",
                                   key = "list-ordering",
                                   value = "badge asc"))
    private List<EventBadge> eventBadges = new ArrayList<EventBadge>();

    @Persistent
    Date startTime;

    @Persistent
    int duration;

    @Persistent
    Date displayStart;

    @Persistent
    Date displayEnd;

    @Persistent
    Date signupsStart;

    @Persistent
    Date signupsEnd;

    @Persistent
    @Element(dependent = "true")
    @Order(extensions = @Extension(
        vendorName = "datanucleus", key = "list-ordering",
        value = "signupTime asc, actualSignupTime asc, character asc"
    ))
    private List<Signup> signups = new ArrayList<Signup>();

    public Event(Key eventTemplate, Key guild, String name, int size,
                 int minimumLevel, String instance, Key instanceKey,
                 Date startTime, int duration, Date displayStart,
                 Date displayEnd, Date signupsStart, Date signupsEnd) {
        this.eventTemplate = eventTemplate;
        this.guild = guild;
        this.name = name;
        this.size = size;
        this.minimumLevel = minimumLevel;
        this.instance = instance;
        this.instanceKey = instanceKey;
        this.startTime = startTime;
        this.duration = duration;
        this.displayStart = displayStart;
        this.displayEnd = displayEnd;
        this.signupsStart = signupsStart;
        this.signupsEnd = signupsEnd;
    }

    public Date getDisplayEnd() {
        return displayEnd;
    }

    public Date getDisplayStart() {
        return displayStart;
    }

    public int getDuration() {
        return duration;
    }

    public List<EventBadge> getEventBadges() {
        return eventBadges;
    }

    public List<EventBoss> getEventBosses() {
        return eventBosses;
    }

    public List<EventRole> getEventRoles() {
        return eventRoles;
    }

    public Key getEventTemplate() {
        return eventTemplate;
    }

    public Key getGuild() {
        return guild;
    }

    public String getInstance() {
        return instance;
    }

    public Key getInstanceKey() {
        return instanceKey;
    }

    public Key getKey() {
        return key;
    }

    public int getMinimumLevel() {
        return minimumLevel;
    }

    public String getName() {
        return name;
    }

    public List<Signup> getSignups() {
        return signups;
    }

    public Date getSignupsEnd() {
        return signupsEnd;
    }

    public Date getSignupsStart() {
        return signupsStart;
    }

    public int getSize() {
        return size;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setDisplayEnd(Date displayEnd) {
        this.displayEnd = displayEnd;
    }

    public void setDisplayStart(Date displayStart) {
        this.displayStart = displayStart;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setEventBadges(List<EventBadge> eventBadges) {
        this.eventBadges = eventBadges;
    }

    public void setEventBosses(List<EventBoss> eventBosses) {
        this.eventBosses = eventBosses;
    }

    public void setEventRoles(List<EventRole> eventRoles) {
        this.eventRoles = eventRoles;
    }

    public void setInstance(String instance, Key instanceKey) {
        this.instance = instance;
        this.instanceKey = instanceKey;
    }

    public void setMinimumLevel(int minimumLevel) {
        this.minimumLevel = minimumLevel;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSignups(List<Signup> signups) {
        this.signups = signups;
    }

    public void setSignupsEnd(Date signupsEnd) {
        this.signupsEnd = signupsEnd;
    }

    public void setSignupsStart(Date signupsStart) {
        this.signupsStart = signupsStart;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }
}
