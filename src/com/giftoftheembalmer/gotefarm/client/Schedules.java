package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

public class Schedules extends Composite {
    Admin admin;
    VerticalPanel vpanel = new VerticalPanel();
    ListBox eventlb = new ListBox();
    List<JSEventTemplate> event_templates;

    public Schedules(Admin admin) {
        this.admin = admin;

        eventlb.setWidth("100%");
        eventlb.setVisibleItemCount(20);

        eventlb.addChangeListener(new ChangeListener() {
            public void onChange(Widget sender) {
                int sel = eventlb.getSelectedIndex();
                if (sel < 0) return;

                String name = eventlb.getItemText(sel);

                GoteFarm.goteService.getEventSchedules(GoteFarm.sessionID, name, new AsyncCallback<List<JSEventSchedule>>() {
                    public void onSuccess(List<JSEventSchedule> results) {
                        Schedules.this.admin.setCenterWidget(new ScheduleEditor(results));
                    }

                    public void onFailure(Throwable caught) {
                    }
                });
            }
        });

        vpanel.add(eventlb);

        initWidget(vpanel);

        setStyleName("Admin-Schedules");
    }

    public void setEventTemplates(List<JSEventTemplate> events) {
        event_templates = events;

        eventlb.clear();

        for (JSEventTemplate e : events) {
            eventlb.addItem(e.name);
        }
    }
}
