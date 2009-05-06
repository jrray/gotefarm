package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.Set;

public class JSGuild implements IsSerializable {

    public String key;
    public String region;
    public String realm;
    public String name;
    public String owner;
    public Set<String> officers;
    public String time_zone;

    public JSGuild() {
    }

    public boolean isOfficer(String account_key) {
        return owner.equals(account_key) || officers.contains(account_key);
    }
}
