package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.List;

public class JSEventTemplate implements IsSerializable {
    public String key;
    public String name;
    public int size;
    public int minimumLevel;
    public String instance_key;
    public List<String> boss_keys;
    public List<JSEventRole> roles;
    public List<JSEventBadge> badges;
    public boolean modifyEvents;

    public JSEventTemplate() {
    }

    @Override
    public boolean equals(Object obj) {
        if ((obj != null) && (obj.getClass().equals(getClass()))) {
            JSEventTemplate o = (JSEventTemplate)obj;
            if (   o.key.equals(key)
                && o.name.equals(name)
                && o.size == size
                && o.minimumLevel == minimumLevel
                && o.instance_key.equals(instance_key)
                && o.boss_keys.equals(boss_keys)
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
        code = 17 * code + key.hashCode();
        code = 17 * code + name.hashCode();
        code = 17 * code + size;
        code = 17 * code + minimumLevel;
        code = 17 * code + instance_key.hashCode();
        code = 17 * code + boss_keys.hashCode();
        code = 17 * code + roles.hashCode();
        code = 17 * code + badges.hashCode();
        code = 17 * code + (modifyEvents ? 1 : 0);
        return code;
    }
}
