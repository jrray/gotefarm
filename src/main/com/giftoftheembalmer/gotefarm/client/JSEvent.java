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

    @Override
    public boolean equals(Object obj) {
        if ((obj != null) && (obj.getClass().equals(getClass()))) {
            if (super.equals(obj) == false) {
                return false;
            }
            JSEvent o = (JSEvent)obj;
            if (   o.start_time.equals(start_time)
                && o.duration == duration
                && o.display_start.equals(display_start)
                && o.display_end.equals(display_end)
                && o.signups_start.equals(signups_start)
                && o.signups_end.equals(signups_end)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int code = 317;
        code = 17 * code + super.hashCode();
        code = 17 * code + start_time.hashCode();
        code = 17 * code + duration;
        code = 17 * code + display_start.hashCode();
        code = 17 * code + display_end.hashCode();
        code = 17 * code + signups_start.hashCode();
        code = 17 * code + signups_end.hashCode();
        return code;
    }
}
