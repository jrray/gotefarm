package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.user.client.rpc.IsSerializable;

public class JSBadge implements IsSerializable, BadgeAndRole {
    public String key;
    public String name;
    public int score;

    public JSBadge() {
    }

    public String getName() {
        return name;
    }

    public String getKey() {
        return key;
    }

    public boolean isRestricted() {
        return true;
    }
}
