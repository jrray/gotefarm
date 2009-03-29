package com.giftoftheembalmer.gotefarm.client;


public class JSChrRole extends JSRole implements ChrBadgeAndRole {
    public long chrroleid;
    public boolean waiting;
    public boolean approved;
    public String message;

    public JSChrRole() {
    }

    public boolean isWaiting() {
        return waiting;
    }

    public boolean isApproved() {
        return approved;
    }

    public String getMessage() {
        return message;
    }
}
