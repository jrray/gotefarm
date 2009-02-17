package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.List;

public class JSEventTemplate implements IsSerializable {
    public long eid;
    public String name;
    public int size;
    public int minimumLevel;
    public String instance;
    public List<String> bosses;
    public List<JSEventRole> roles;
    public List<JSEventBadge> badges;
    public boolean modifyEvents;

    public JSEventTemplate() {
    }
}
