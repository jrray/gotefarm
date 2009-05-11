package com.giftoftheembalmer.gotefarm.server.dao;

import com.google.appengine.api.datastore.Key;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import java.util.Date;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Signup {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;

    @Persistent
    private Key event;

    @Persistent
    private Key character;

    @Persistent
    private Key role;

    @Persistent
    private int signupType;

    @Persistent
    private Date signupTime;

    @Persistent
    private Date actualSignupTime;

    @Persistent
    private String note;

    public Signup(Key event, Key character, Key role, int signupType,
                  Date signupTime, String note) {
        this.event = event;
        this.character = character;
        this.role = role;
        this.signupType = signupType;
        setSignupTime(signupTime);
        this.note = note;
    }

    public Date getActualSignupTime() {
        return actualSignupTime;
    }

    public Key getCharacter() {
        return character;
    }

    public Key getEvent() {
        return event;
    }

    public Key getKey() {
        return key;
    }

    public String getNote() {
        return note;
    }

    public Key getRole() {
        return role;
    }

    public int getSignupType() {
        return signupType;
    }

    public Date getSignupTime() {
        return signupTime;
    }

    public void setRole(Key role) {
        this.role = role;
    }

    public void setSignupType(int signupType) {
        this.signupType = signupType;
    }

    public void setSignupTime(Date signupTime) {
        this.signupTime = signupTime;
        this.actualSignupTime = new Date();
    }
}
