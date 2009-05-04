package com.giftoftheembalmer.gotefarm.server.dao;

import com.google.appengine.api.datastore.Key;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Role {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;

    @Persistent
    private Key guild;

    @Persistent
    private String name;

    @Persistent
    private boolean restricted;

    public Role(Key guild, String name, boolean restricted) {
        this.guild = guild;
        this.name = name;
        this.restricted = restricted;
    }

    public Key getGuild() {
        return guild;
    }

    public Key getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public boolean getRestricted() {
        return restricted;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRestricted(boolean restricted) {
        this.restricted = restricted;
    }
}
