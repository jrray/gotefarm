package com.giftoftheembalmer.gotefarm.client;

public class JSChrBadge extends JSBadge implements ChrBadgeAndRole {
    public long chrbadgeid;
    public boolean waiting;
    public boolean approved;
    public String message;

    public JSChrBadge() {
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
