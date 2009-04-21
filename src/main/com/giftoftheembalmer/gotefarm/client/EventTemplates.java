package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.List;

public class EventTemplates extends Composite {
    Admin admin;
    VerticalPanel vpanel = new VerticalPanel();
    ListBox eventlb = new ListBox();
    List<JSEventTemplate> event_templates;

    public EventTemplates(Admin admin) {
        this.admin = admin;

        eventlb.setWidth("100%");
        eventlb.setVisibleItemCount(20);

        eventlb.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                int sel = eventlb.getSelectedIndex();
                if (sel < 0) return;

                String name = eventlb.getItemText(sel);

                for (JSEventTemplate e : event_templates) {
                    if (e.name.equals(name)) {
                        EventTemplates.this.admin.setCenterWidget(new EventEditor(EventTemplates.this.admin, e));
                        return;
                    }
                }

                // TODO: display not-found error
                EventTemplates.this.admin.setCenterWidget(null);
            }
        });

        vpanel.add(eventlb);

        vpanel.add(new Button("New Event", new ClickHandler() {
            public void onClick(ClickEvent event) {
                eventlb.setSelectedIndex(-1);
                EventTemplates.this.admin.setCenterWidget(new EventEditor(EventTemplates.this.admin));
            }
        }));

        initWidget(vpanel);

        setStyleName("Admin-EventTemplates");
    }

    public void setEventTemplates(List<JSEventTemplate> events) {
        event_templates = events;

        eventlb.clear();

        for (JSEventTemplate e : events) {
            eventlb.addItem(e.name);
        }
    }
}
