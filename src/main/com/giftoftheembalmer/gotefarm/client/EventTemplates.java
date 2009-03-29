package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

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

        eventlb.addChangeListener(new ChangeListener() {
            public void onChange(Widget sender) {
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

        vpanel.add(new Button("New Event", new ClickListener() {
            public void onClick(Widget sender) {
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
