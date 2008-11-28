package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.user.client.rpc.IsSerializable;

public class JSRole implements IsSerializable {
    public int roleid;
    public String name;
    public boolean restricted;

    public JSRole() {
    }
}
