package com.giftoftheembalmer.gotefarm.server.dao;

import com.google.appengine.api.datastore.Key;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class EventBoss implements Copyable<EventBoss> {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;

    @Persistent
    private String boss;

    @Persistent
    private Key bossKey;

    public EventBoss(String boss, Key bossKey) {
        this.boss = boss;
        this.bossKey = bossKey;
    }

    public EventBoss copy() {
        return new EventBoss(boss, bossKey);
    }

    public String getBoss() {
        return boss;
    }

    public Key getBossKey() {
        return bossKey;
    }

    public Key getKey() {
        return key;
    }

    public void setBoss(String boss, Key bossKey) {
        this.boss = boss;
        this.bossKey = bossKey;
    }
}
