package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;

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

        eventlb.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                int sel = eventlb.getSelectedIndex();
                if (sel < 0) return;

                final String name = eventlb.getItemText(sel);

                GoteFarm.goteService.getEventSchedules(GoteFarm.sessionID, name, new AsyncCallback<List<JSEventSchedule>>() {
                    public void onSuccess(List<JSEventSchedule> results) {
                        long eid = -1L;

                        for (JSEventTemplate e : Schedules.this.event_templates) {
                            if (e.name.equals(name)) {
                                eid = e.eid;
                                break;
                            }
                        }

                        Schedules.this.admin.setCenterWidget(new ScheduleEditor(eid, results));
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
