package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.user.client.rpc.IsSerializable;

public class JSBadge implements IsSerializable, BadgeAndRole {
    public long badgeid;
    public String name;
    public int score;

    public JSBadge() {
    }

    public String getName() {
        return name;
    }

    public long getId() {
        return badgeid;
    }

    public boolean isRestricted() {
        return true;
    }
}
