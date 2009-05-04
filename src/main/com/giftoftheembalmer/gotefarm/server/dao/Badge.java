package com.giftoftheembalmer.gotefarm.server.dao;

import com.google.appengine.api.datastore.Key;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Badge {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;

    @Persistent
    private Key guild;

    @Persistent
    private String name;

    @Persistent
    private int score;

    public Badge(Key guild, String name, int score) {
        this.guild = guild;
        this.name = name;
        this.score = score;
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

    public int getScore() {
        return score;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
