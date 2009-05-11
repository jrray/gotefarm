package com.giftoftheembalmer.gotefarm.client;

public class AdminChange {

    public boolean events_changed;

    public static AdminChange getEventsChanged() {
        AdminChange ac = new AdminChange();
        ac.events_changed = true;
        return ac;
    }
}
