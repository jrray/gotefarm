package com.giftoftheembalmer.gotefarm.server.dao;

import com.google.appengine.api.datastore.Key;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class ChrBadge {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;

    @Persistent
    private String badge;

    @Persistent
    private Key badgeKey;

    @Persistent
    private boolean waiting;

    @Persistent
    private boolean approved;

    @Persistent
    private String message;

    @Persistent
    private Key messageFrom;

    public ChrBadge(String badge, Key badgeKey, boolean waiting,
                   boolean approved) {
        this.badge = badge;
        this.badgeKey = badgeKey;
        this.waiting = waiting;
        this.approved = approved;
    }

    public String getBadge() {
        return badge;
    }

    public Key getBadgeKey() {
        return badgeKey;
    }

    public Key getKey() {
        return key;
    }

    public String getMessage() {
        return message;
    }

    public Key getMessageFrom() {
        return messageFrom;
    }

    public boolean isApproved() {
        return approved;
    }

    public boolean isWaiting() {
        return waiting;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public void setBadge(String badge) {
        this.badge = badge;
    }

    public void setMessage(String message, Key messageFrom) {
        this.message = message;
        this.messageFrom = messageFrom;
    }

    public void setWaiting(boolean waiting) {
        this.waiting = waiting;
    }
}
