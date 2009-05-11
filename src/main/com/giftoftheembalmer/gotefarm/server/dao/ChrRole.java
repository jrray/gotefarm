package com.giftoftheembalmer.gotefarm.server.dao;

import com.google.appengine.api.datastore.Key;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class ChrRole {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;

    @Persistent
    private String role;

    @Persistent
    private Key roleKey;

    @Persistent
    private boolean waiting;

    @Persistent
    private boolean approved;

    @Persistent
    private String message;

    @Persistent
    private Key messageFrom;

    public ChrRole(String role, Key roleKey, boolean waiting,
                   boolean approved) {
        this.role = role;
        this.roleKey = roleKey;
        this.waiting = waiting;
        this.approved = approved;
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

    public String getRole() {
        return role;
    }

    public Key getRoleKey() {
        return roleKey;
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

    public void setMessage(String message, Key messageFrom) {
        this.message = message;
        this.messageFrom = messageFrom;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setWaiting(boolean waiting) {
        this.waiting = waiting;
    }
}
