package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.user.client.rpc.IsSerializable;

public class JSRole implements IsSerializable, BadgeAndRole {
    public long roleid;
    public String name;
    public boolean restricted;

    public JSRole() {
    }

    public String getName() {
        return name;
    }

    public long getId() {
        return roleid;
    }

    public boolean isRestricted() {
        return restricted;
    }
}
