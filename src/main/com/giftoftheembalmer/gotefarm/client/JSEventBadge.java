package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.user.client.rpc.IsSerializable;

public class JSEventBadge implements IsSerializable {
    public long badgeid;
    public String name;
    public boolean requireForSignup;
    public String applyToRole;
    public int numSlots;
    public int earlySignup;

    public JSEventBadge() {
    }

    @Override
    public boolean equals(Object obj) {
        if ((obj != null) && (obj.getClass().equals(getClass()))) {
            JSEventBadge o = (JSEventBadge)obj;
            if (   o.badgeid == badgeid
                && o.name.equals(name)
                && o.requireForSignup == requireForSignup
                && ((   o.applyToRole == null && applyToRole == null)
                     || o.applyToRole.equals(applyToRole))
                && o.numSlots == numSlots
                && o.earlySignup == earlySignup) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int code = 317;
        code = 17 * code + (int)(badgeid^(badgeid >>> 32));
        code = 17 * code + name.hashCode();
        code = 17 * code + (requireForSignup ? 1 : 0);
        code = 17 * code + (applyToRole == null ? 0 : applyToRole.hashCode());
        code = 17 * code + numSlots;
        code = 17 * code + earlySignup;
        return code;
    }
}
