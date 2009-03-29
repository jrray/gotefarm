package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.user.client.ui.FlexTable;

abstract class BadgeAndRoleClickListenerFactory {
    public abstract
    BadgeAndRoleClickListener newClickListener(FlexTable flex, int row,
                                               ChrBadgeAndRole role);
}
