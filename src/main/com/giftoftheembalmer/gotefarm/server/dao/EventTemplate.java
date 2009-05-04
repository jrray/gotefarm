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

import java.util.List;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class EventTemplate {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;

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
    @Order(extensions = @Extension(vendorName="datanucleus", key="list-ordering", value="boss asc"))
    private List<EventBoss> bosses;

    // FIXME: Using List here due to AppEngine bug, switch to Set once
    // it is fixed.
    // http://code.google.com/p/datanucleus-appengine/issues/detail?id=26
    @Persistent
    @Element(dependent = "true")
    @Order(extensions = @Extension(vendorName="datanucleus", key="list-ordering", value="role asc"))
    private List<EventRole> roles;

    // FIXME: Using List here due to AppEngine bug, switch to Set once
    // it is fixed.
    // http://code.google.com/p/datanucleus-appengine/issues/detail?id=26
    @Persistent
    @Element(dependent = "true")
    @Order(extensions = @Extension(vendorName="datanucleus", key="list-ordering", value="badge asc"))
    private List<EventBadge> badges;

    public EventTemplate(Key guild, String name, int size, int minimumLevel,
                         String instance, Key instanceKey) {
        this.guild = guild;
        this.name = name;
        this.size = size;
        this.minimumLevel = minimumLevel;
        this.instance = instance;
        this.instanceKey = instanceKey;
    }

    public List<EventBadge> getBadges() {
        return badges;
    }

    public List<EventBoss> getBosses() {
        return bosses;
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

    public List<EventRole> getRoles() {
        return roles;
    }

    public int getSize() {
        return size;
    }

    public void setBadges(List<EventBadge> badges) {
        this.badges = badges;
    }

    public void setBosses(List<EventBoss> bosses) {
        this.bosses = bosses;
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

    public void setRoles(List<EventRole> roles) {
        this.roles = roles;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
