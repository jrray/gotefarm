package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.Date;
import java.util.List;

public class JSEventSignups implements IsSerializable {
    public long eventid;
    public List<JSEventSignup> signups;
    public Date asof;

    public JSEventSignups() {
    }
}
