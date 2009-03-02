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

    @Override
    public boolean equals(Object obj) {
        if ((obj != null) && (obj.getClass().equals(getClass()))) {
            JSEventTemplate o = (JSEventTemplate)obj;
            if (   o.eid == eid
                && o.name.equals(name)
                && o.size == size
                && o.minimumLevel == minimumLevel
                && o.instance.equals(instance)
                && o.bosses.equals(bosses)
                && o.roles.equals(roles)
                && o.badges.equals(badges)
                && o.modifyEvents == modifyEvents) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int code = 317;
        code = 17 * code + (int)(eid^(eid >>> 32));
        code = 17 * code + name.hashCode();
        code = 17 * code + size;
        code = 17 * code + minimumLevel;
        code = 17 * code + instance.hashCode();
        code = 17 * code + bosses.hashCode();
        code = 17 * code + roles.hashCode();
        code = 17 * code + badges.hashCode();
        code = 17 * code + (modifyEvents ? 1 : 0);
        return code;
    }
}
