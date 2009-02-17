package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.user.client.rpc.IsSerializable;

public class JSBadge implements IsSerializable {
    public long badgeid;
    public String name;
    public int score;

    public JSBadge() {
    }
}
