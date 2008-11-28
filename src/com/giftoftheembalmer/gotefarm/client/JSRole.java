package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.user.client.rpc.IsSerializable;

public class JSRole implements IsSerializable {
    public long roleid;
    public String name;
    public boolean restricted;

    public JSRole() {
    }
}
