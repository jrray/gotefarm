package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.StackPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;
import java.util.ArrayList;

public class Admin extends Composite
    implements ValueChangeHandler<JSGuild>,
               HasValueChangeHandlers<AdminChange> {

    Widget centerWidget = null;
    DockPanel dpanel = new DockPanel();
    List<JSEventTemplate> event_templates;
    JSGuild current_guild;

    EventTemplates events;
    Schedules schedules;

    public Admin() {
        events = new EventTemplates(this);
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

        dpanel.add(sp, DockPanel.WEST);

        initWidget(dpanel);

        setStyleName("Admin");

        getEventTemplates();
    }

    public void setCenterWidget(Widget widget) {
        if (centerWidget != null) {
            dpanel.remove(centerWidget);
        }

        if (widget != null) {
            dpanel.add(widget, DockPanel.CENTER);
        }

        centerWidget = widget;
    }

    // called from GoteFarm after user logs in
    public void refresh() {
        getEventTemplates();
    }

    public void eventAdded() {
        // reload event templates
        getEventTemplates();
    }

    void getEventTemplates() {
        if (current_guild == null) {
            return;
        }

        GoteFarm.goteService.getEventTemplates(
            current_guild.key,
            new AsyncCallback<List<JSEventTemplate>>() {

            public void onSuccess(List<JSEventTemplate> results) {
                event_templates = results;

                // notify sub-widgets of new list
                events.setEventTemplates(event_templates);
                schedules.setEventTemplates(event_templates);
            }

            public void onFailure(Throwable caught) {
                List<JSEventTemplate> l = new ArrayList<JSEventTemplate>();
                events.setEventTemplates(l);
                schedules.setEventTemplates(l);
            }
        });
    }

    public void onValueChange(ValueChangeEvent<JSGuild> event) {
        current_guild = event.getValue();
        // discard any current activity
        setCenterWidget(null);
        refresh();
    }

    public HandlerRegistration addValueChangeHandler(
            ValueChangeHandler<AdminChange> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    public void fireAdminChange(AdminChange adminChange) {
        ValueChangeEvent.fire(this, adminChange);
    }
}
