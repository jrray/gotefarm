package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.SimpleCheckBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.Window;

abstract class BadgeAndRoleClickListener implements ClickListener {
    String flavor1; // e.g., role
    private FlexTable flex;
    private int row;
    protected ChrBadgeAndRole role;

    public BadgeAndRoleClickListener(String flavor1, FlexTable flex, int row, ChrBadgeAndRole role) {
        this.flavor1 = flavor1;
        this.flex = flex;
        this.row = row;
        this.role = role;
    }

    public void onClick(Widget sender) {
        SimpleCheckBox hasrole = (SimpleCheckBox)sender;

        boolean adding = true;
        if (hasrole.isChecked()) {
            if (role.isRestricted()) {
                flex.setText(row, 2, "pending admin approval");
            }
        }
        else {
            adding = false;
            flex.setText(row, 2, "");

            // user is unchecking a role, confirm this action
            // if the role is restricted
            if (role.isRestricted()) {
                if (role.isApproved()) {
                    if (!Window.confirm("This " + flavor1 + " will need to be approved by an admin if you decide to add it back, are you sure you want to remove it?")) {
                        hasrole.setChecked(true);
                        return;
                    }
                }
            }
        }

        updateCharacterRole(adding);
    }

    public abstract void updateCharacterRole(boolean adding);
}
