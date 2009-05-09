package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.Date;

public class JSCharacter implements IsSerializable {
    public long accountid;
    public long cid;
    public String name;
    public boolean main;
    public String race;
    public String clazz;
    public short level;
    public String characterxml;
    public Date created;
    public JSChrRole[] roles;
    public JSChrBadge[] badges;

    public JSCharacter() {
    }

    public boolean hasBadge(String key) {
        for (JSChrBadge badge : badges) {
            if (badge.key.equals(key)) {
                return badge.approved;
            }
        }

        return false;
    }

    public boolean hasRole(String key) {
        for (JSChrRole role : roles) {
            if (role.key.equals(key)) {
                return role.approved;
            }
        }

        return false;
    }

    public boolean hasRoleName(String name) {
        for (JSChrRole role : roles) {
            if (role.name.equals(name)) {
                return role.approved;
            }
        }

        return false;
    }
}
