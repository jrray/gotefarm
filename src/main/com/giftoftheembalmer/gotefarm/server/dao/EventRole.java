package com.giftoftheembalmer.gotefarm.server.dao;

import com.google.appengine.api.datastore.Key;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class EventRole {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;

    @Persistent
    private String role;

    @Persistent
    private Key roleKey;

    @Persistent
    int min;

    @Persistent
    int max;

    public EventRole(String role, Key roleKey, int min, int max) {
        if (role == null) {
            throw new IllegalArgumentException("Role must not be null");
        }
        if (min < 0) {
            throw new IllegalArgumentException("Min must be positive");
        }
        if (max < min) {
            throw new IllegalArgumentException(
                "Max must not be less than min"
            );
        }
        if (max < 1) {
            throw new IllegalArgumentException(
                "Max must be greater than zero"
            );
        }
        this.role = role;
        this.roleKey = roleKey;
        this.min = min;
        this.max = max;
    }

    public Key getKey() {
        return key;
    }

    public int getMax() {
        return max;
    }

    public int getMin() {
        return min;
    }

    public String getRole() {
        return role;
    }

    public Key getRoleKey() {
        return roleKey;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public void setRole(String role, Key roleKey) {
        this.role = role;
        this.roleKey = roleKey;
    }
}
