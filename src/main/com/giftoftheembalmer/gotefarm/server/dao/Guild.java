package com.giftoftheembalmer.gotefarm.server.dao;

import com.google.appengine.api.datastore.Key;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Guild {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;

    @Persistent
    private String region;

    @Persistent
    private String realm;

    @Persistent
    private String name;

    @Persistent
    private Key owner;

    @Persistent
    private Set<Key> officers;

    @Persistent
    private Date lastUpdate;

    @Persistent
    private String timeZone;

    public Guild(String region, String realm, String name, Key owner,
                 String timeZone) {
        this.region = region;
        this.realm = realm;
        this.name = name;
        this.owner = owner;
        officers = new HashSet<Key>();
        lastUpdate = new Date();
        this.timeZone = timeZone;
    }

    public Key getKey() {
        return key;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public String getName() {
        return name;
    }

    public Set<Key> getOfficers() {
        return officers;
    }

    public Key getOwner() {
        return owner;
    }

    public String getRealm() {
        return realm;
    }

    public String getRegion() {
        return region;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOwner(Key owner) {
        this.owner = owner;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }
}
