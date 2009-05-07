package com.giftoftheembalmer.gotefarm.server.dao;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;

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
    private ChrGroup chrGroup;

    @Persistent
    private Key guild;

    @Persistent
    private String name;

    @Persistent
    private String race;

    @Persistent
    private Key raceKey;

    @Persistent
    private String clazz;

    @Persistent
    private Key classKey;

    @Persistent
    private int level;

    @Persistent
    private Text chrxml;

    @Persistent
    private Date created;

    public Chr(ChrGroup chrGroup, Key guild, String name, String race,
               Key raceKey, String clazz, Key classKey, int level,
               String chrxml, Date created) {
        this.chrGroup = chrGroup;
        this.guild = guild;
        this.name = name;
        this.race = race;
        this.raceKey = raceKey;
        this.clazz = clazz;
        this.classKey = classKey;
        this.level = level;
        this.chrxml = new Text(chrxml);
        this.created = created;
    }

    public String getChrClass() {
        return clazz;
    }

    public Key getChrClassKey() {
        return classKey;
    }

    public ChrGroup getChrGroup() {
        return chrGroup;
    }

    public String getChrXml() {
        return chrxml.getValue();
    }

    public Date getCreated() {
        return created;
    }

    public Key getGuild() {
        return guild;
    }

    public Key getKey() {
        return key;
    }

    public int getLevel() {
        return level;
    }

    public String getName() {
        return name;
    }

    public String getRace() {
        return race;
    }

    public Key getRaceKey() {
        return raceKey;
    }

    public void setChrClass(String clazz, Key classKey) {
        this.clazz = clazz;
        this.classKey = classKey;
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

    public void setRace(String race, Key raceKey) {
        this.race = race;
        this.raceKey = raceKey;
    }
}
