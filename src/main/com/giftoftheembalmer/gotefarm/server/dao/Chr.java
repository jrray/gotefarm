package com.giftoftheembalmer.gotefarm.server.dao;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.users.User;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import java.util.Date;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Chr {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;

    @Persistent
    private User user;

    @Persistent
    private Key guild;

    @Persistent
    private String name;

    @Persistent
    private Key race;

    @Persistent
    private Key clazz;

    @Persistent
    private int level;

    @Persistent
    private Text chrxml;

    @Persistent
    private Date created;

    public Chr(User user, Key guild, String name, Key race, Key clazz,
               int level, String chrxml, Date created) {
        this.user = user;
        this.guild = guild;
        this.name = name;
        this.race = race;
        this.clazz = clazz;
        this.level = level;
        this.chrxml = new Text(chrxml);
        this.created = created;
    }

    public Key getChrClass() {
        return clazz;
    }

    public String getChrXml() {
        return chrxml.getValue();
    }

    public Date getCreated() {
        return created;
    }

    public Key getKey() {
        return key;
    }

    public int getLevel() {
        return level;
    }

    public Key getGuild() {
        return guild;
    }

    public String getName() {
        return name;
    }

    public Key getRace() {
        return race;
    }

    public User getUser() {
        return user;
    }

    public void setChrClass(Key clazz) {
        this.clazz = clazz;
    }

    public void setChrXml(String chrxml) {
        this.chrxml = new Text(chrxml);
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRace(Key race) {
        this.race = race;
    }
}
