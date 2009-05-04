package com.giftoftheembalmer.gotefarm.server.dao;

import com.google.appengine.api.datastore.Key;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Order;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import java.util.ArrayList;
import java.util.List;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Instance {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;

    @Persistent
    private Key guild;

    @Persistent
    private String name;

    // FIXME: Using List here due to AppEngine bug, switch to Set once
    // it is fixed.
    // http://code.google.com/p/datanucleus-appengine/issues/detail?id=26
    @Persistent
    @Order(extensions = @Extension(vendorName="datanucleus", key="list-ordering", value="name asc"))
    private List<Boss> bosses;

    public Instance(Key guild, String name) {
        this.guild = guild;
        this.name = name;
        bosses = new ArrayList<Boss>();
    }

    public List<Boss> getBosses() {
        return bosses;
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

    public void setBosses(List<Boss> bosses) {
        this.bosses = bosses;
    }

    public void setName(String name) {
        this.name = name;
    }
}
