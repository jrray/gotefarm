package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.Date;

public class JSCharacter implements IsSerializable {
    public int cid;
    public String realm;
    public String name;
    public String race;
    public String clazz;
    public String characterxml;
    public Date created;

    public JSCharacter() {
    }
}
