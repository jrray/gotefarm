package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.Date;

public class JSEventSignup implements IsSerializable {
    public long eventsignupid;
    public long eventid;
    public JSCharacter chr;
    public JSRole role;
    public int signup_type;
    public Date signup_time;
    public String note;

    public JSEventSignup() {
    }
}
