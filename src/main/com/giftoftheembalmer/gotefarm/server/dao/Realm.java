package com.giftoftheembalmer.gotefarm.server.dao;

import com.google.appengine.api.datastore.Key;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Realm {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;

    @Persistent
    private String region;

    @Persistent
    private String name;

    @Persistent
    private String timezone;

    public Realm(String region, String name, String timezone) {
        this.region = region;
        this.name = name;
        this.timezone = timezone;
    }

    public Key getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getRegion() {
        return region;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
}
