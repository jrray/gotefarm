package com.giftoftheembalmer.gotefarm.server.dao;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;

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
public class Chr {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;

    @Persistent
    private Key accountKey;

    @Persistent
    private ChrGroup chrGroup;

    @Persistent
    private Key guild;

    @Persistent
    private String name;

    @Persistent
    private boolean main;

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

    // FIXME: Using List here due to AppEngine bug, switch to Set once
    // it is fixed.
    // http://code.google.com/p/datanucleus-appengine/issues/detail?id=26
    @Persistent
    @Element(dependent = "true")
    @Order(extensions = @Extension(vendorName = "datanucleus",
                                   key = "list-ordering", value = "role asc"))
    private List<ChrRole> roles = new ArrayList<ChrRole>();

    public Chr(Key accountKey, ChrGroup chrGroup, Key guild, String name,
               boolean main, String race, Key raceKey, String clazz,
               Key classKey, int level, String chrxml, Date created) {
        this.accountKey = accountKey;
        this.chrGroup = chrGroup;
        this.guild = guild;
        this.name = name;
        this.main = main;
        this.race = race;
        this.raceKey = raceKey;
        this.clazz = clazz;
        this.classKey = classKey;
        this.level = level;
        this.chrxml = new Text(chrxml);
        this.created = created;
    }

    public Key getAccountKey() {
        return accountKey;
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

    public boolean getMain() {
        return main;
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

    public List<ChrRole> getRoles() {
        return roles;
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

    public void setMain(boolean main) {
        this.main = main;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRace(String race, Key raceKey) {
        this.race = race;
        this.raceKey = raceKey;
    }

    public void setRoles(List<ChrRole> roles) {
        this.roles = roles;
    }
}
