package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.user.client.rpc.IsSerializable;

public class JSEventRole implements IsSerializable {
    public long roleid;
    public String name;
    public int min;
    public int max;

    public JSEventRole() {
    }

    @Override
    public boolean equals(Object obj) {
        if ((obj != null) && (obj.getClass().equals(getClass()))) {
            JSEventRole o = (JSEventRole)obj;
            if (   o.roleid == roleid
                && o.name.equals(name)
                && o.min == min
                && o.max == max) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int code = 317;
        code = 17 * code + (int)(roleid^(roleid >>> 32));
        code = 17 * code + name.hashCode();
        code = 17 * code + min;
        code = 17 * code + max;
        return code;
    }
}
