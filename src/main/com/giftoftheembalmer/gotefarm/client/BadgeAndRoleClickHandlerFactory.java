package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.user.client.ui.FlexTable;

abstract class BadgeAndRoleClickHandlerFactory {
    public abstract
    BadgeAndRoleClickHandler newClickHandler(FlexTable flex, int row,
                                             ChrBadgeAndRole role);
}
