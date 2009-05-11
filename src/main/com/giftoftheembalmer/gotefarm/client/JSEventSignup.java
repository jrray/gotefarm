package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.Date;

public class JSEventSignup implements IsSerializable {
    static final int SIGNUP_TYPE_COMING = 1;
    static final int SIGNUP_TYPE_MAYBE = 2;
    static final int SIGNUP_TYPE_NOT_COMING = 3;
    static final String SIGNUP_LABEL_COMING = "Coming";
    static final String SIGNUP_LABEL_MAYBE = "Maybe";
    static final String SIGNUP_LABEL_NOT_COMING = "Not Coming";

    public String key;
    public String event_key;
    public JSCharacter chr;
    public JSRole role;
    public int signup_type;
    public Date signup_time;
    public String note;

    public JSEventSignup() {
    }
}
