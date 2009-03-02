package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.user.client.rpc.IsSerializable;

public class JSEventBadge implements IsSerializable {
    public long badgeid;
    public String name;
    public boolean requireForSignup;
    public String applyToRole;
    public int numSlots;
    public int earlySignup;

    public JSEventBadge() {
    }
}
