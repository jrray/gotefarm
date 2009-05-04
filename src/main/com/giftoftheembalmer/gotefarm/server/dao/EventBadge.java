package com.giftoftheembalmer.gotefarm.server.dao;

import com.google.appengine.api.datastore.Key;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class EventBadge {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;

    @Persistent
    private String badge;

    @Persistent
    private Key badgeKey;

    @Persistent
    boolean requireForSignup;

    @Persistent
    String applyToRole;

    @Persistent
    int numSlots;

    @Persistent
    int earlySignup;

    public EventBadge(String badge, Key badgeKey, boolean requireForSignup,
                      String applyToRole, int numSlots, int earlySignup) {
        if (badge == null) {
            throw new IllegalArgumentException("Badge must not be null");
        }
        if (numSlots < 0) {
            throw new IllegalArgumentException("NumSlots must be positive");
        }
        if (earlySignup < 0) {
            throw new IllegalArgumentException("EarlySignup must be positive");
        }
        this.badge = badge;
        this.badgeKey = badgeKey;
        this.requireForSignup = requireForSignup;
        this.applyToRole = applyToRole;
        this.numSlots = numSlots;
        this.earlySignup = earlySignup;
    }

    public String getApplyToRole() {
        return applyToRole;
    }

    public String getBadge() {
        return badge;
    }

    public Key getBadgeKey() {
        return badgeKey;
    }

    public int getEarlySignup() {
        return earlySignup;
    }

    public Key getKey() {
        return key;
    }

    public int getNumSlots() {
        return numSlots;
    }

    public boolean getRequireForSignup() {
        return requireForSignup;
    }

    public void setApplyToRole(String applyToRole) {
        this.applyToRole = applyToRole;
    }

    public void setBadge(String badge, Key badgeKey) {
        this.badge = badge;
        this.badgeKey = badgeKey;
    }

    public void setEarlySignup(int earlySignup) {
        this.earlySignup = earlySignup;
    }

    public void setNumSlots(int numSlots) {
        this.numSlots = numSlots;
    }

    public void setRequireForSignup(boolean requireForSignup) {
        this.requireForSignup = requireForSignup;
    }
}
