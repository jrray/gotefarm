package com.giftoftheembalmer.gotefarm.server.dao;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.users.User;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import java.util.Date;
import java.util.Set;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Account {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;

    @Persistent
    private User user;

    @Persistent
    private Set<Key> guilds;

    @Persistent
    private Key activeGuild;

    @Persistent
    private Date lastUpdate;

    public Account(User user) {
        this.user = user;
        lastUpdate = new Date();
    }

    public Key getActiveGuild() {
        return activeGuild;
    }

    public Set<Key> getGuilds() {
        return guilds;
    }

    public Key getKey() {
        return key;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public User getUser() {
        return user;
    }

    public void setActiveGuild(Key activeGuild) {
        this.activeGuild = activeGuild;
    }

    public void setGuilds(Set<Key> guilds) {
        this.guilds = guilds;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
