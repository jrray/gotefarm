package com.giftoftheembalmer.gotefarm.server.dao;

import com.google.appengine.api.datastore.Key;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Region {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;

    @Persistent
    private String code;

    public Region(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public Key getKey() {
        return key;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
