package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

public class Events extends Composite {
    Admin admin;
    VerticalPanel vpanel = new VerticalPanel();
    ListBox eventlb = new ListBox();

    public Events(Admin admin) {
        this.admin = admin;
        eventlb.setWidth("100%");
        eventlb.setVisibleItemCount(20);

        eventlb.addChangeListener(new ChangeListener() {
            public void onChange(Widget sender) {
                int sel = eventlb.getSelectedIndex();
                if (sel < 0) return;

                String name = eventlb.getItemText(sel);

                GoteFarm.testService.getEventTemplate(GoteFarm.sessionID, name, new AsyncCallback<JSEventTemplate>() {
                    public void onSuccess(JSEventTemplate result) {
                        Events.this.admin.setCenterWidget(new EventEditor(Events.this.admin, result));
                    }

                    public void onFailure(Throwable caught) {
                    }
                });
            }
        });

        vpanel.add(eventlb);

        vpanel.add(new Button("New Event", new ClickListener() {
            public void onClick(Widget sender) {
                eventlb.setSelectedIndex(-1);
                Events.this.admin.setCenterWidget(new EventEditor(Events.this.admin));
            }
        }));

        update();

        initWidget(vpanel);

        setStyleName("Admin-Events");
    }

    public void update() {
        eventlb.clear();
        GoteFarm.testService.getEventTemplates(GoteFarm.sessionID, new AsyncCallback<List<String>>() {
            public void onSuccess(List<String> results) {
                for (String t : results) {
                    eventlb.addItem(t);
                }
            }

            public void onFailure(Throwable caught) {
            }
        });
    }
}
