package com.giftoftheembalmer.gotefarm.client;

public interface ChrBadgeAndRole extends BadgeAndRole {
    public boolean isWaiting();
    public boolean isApproved();
    public String getMessage();
}
