package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.Date;

public class JSEvent extends JSEventTemplate implements IsSerializable {
    public Date start_time;
    public int duration;
    public Date display_start;
    public Date display_end;
    public Date signups_start;
    public Date signups_end;

    public JSEvent() {
    }
}
