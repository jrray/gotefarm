package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.StackPanel;
import com.google.gwt.user.client.ui.Widget;

public class Admin extends Composite {

    Widget centerWidget = null;
    DockPanel dpanel = new DockPanel();

    Events events;
    Schedules schedules;

    public void eventAdded() {
        events.update();
        schedules.update();
    }

    public Admin() {
        events = new Events(this);
        schedules = new Schedules(this);

        dpanel.setWidth("100%");

        StackPanel sp = new StackPanel();

        sp.add(events, "Events");
        sp.add(schedules, "Schedules");
        sp.add(new Label("Accounts"), "Accounts");
        sp.add(new Label("Instances"), "Instances");
        sp.add(new Label("Bosses"), "Bosses");
        sp.add(new Label("Roles"), "Roles");
        sp.add(new Label("Badges"), "Badges");

        dpanel.add(sp, dpanel.WEST);

        initWidget(dpanel);

        setStyleName("Admin");
    }

    public void setCenterWidget(Widget widget) {
        if (centerWidget != null) {
            dpanel.remove(centerWidget);
        }

        if (widget != null) {
            dpanel.add(widget, dpanel.CENTER);
        }

        centerWidget = widget;
    }

    public void refresh() {
        events.update();
        schedules.update();
    }
}
