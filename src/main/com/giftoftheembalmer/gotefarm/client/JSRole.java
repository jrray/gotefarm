package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.user.client.rpc.IsSerializable;

public class JSRole implements IsSerializable, BadgeAndRole {
    public String key;
    public String name;
    public boolean restricted;

    public JSRole() {
    }

    public String getName() {
        return name;
    }

    public String getKey() {
        return key;
    }

    public boolean isRestricted() {
        return restricted;
    }
}
