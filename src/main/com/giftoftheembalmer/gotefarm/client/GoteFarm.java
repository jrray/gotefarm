package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class GoteFarm implements EntryPoint, ValueChangeHandler<String>, SelectionHandler<Integer> {

  static GoteFarmRPCAsync goteService = null;
  static String sessionID = null;

  TabPanel tabpanel;
  Guilds guilds;
  Events events;
  Characters chars;
  Admin admin;

  /**
   * This is the entry point method.
   */
  public void onModuleLoad() {
    goteService = (GoteFarmRPCAsync)GWT.create(GoteFarmRPC.class);

    ServiceDefTarget endpoint = (ServiceDefTarget)goteService;
    String moduleRelativeURL = GWT.getModuleBaseURL() + "rpc/goteService";
    endpoint.setServiceEntryPoint(moduleRelativeURL);

    tabpanel = new TabPanel();
    guilds = new Guilds();
    events = new Events();
    chars = new Characters();
    admin = new Admin();

    // Events wants to know when there are changes to the user's characters
    chars.addValueChangeHandler(events);
    // Characters wants to know when the current guild changes
    guilds.addValueChangeHandler(chars);

    final VerticalPanel vpanel = new VerticalPanel();
    vpanel.setWidth("100%");

    setSessionID("work in progress");

    tabpanel.setWidth("98%");
    tabpanel.addStyleName(tabpanel.getStylePrimaryName() + "-main");
    tabpanel.addSelectionHandler(this);

    tabpanel.add(guilds, "Guilds");
    tabpanel.add(events, "Events");
    tabpanel.add(chars, "Characters");
    tabpanel.add(admin, "Admin");

    vpanel.add(tabpanel);

    RootPanel.get().add(vpanel);

    String initToken = History.getToken();
    if (initToken.length() == 0) {
        History.newItem("events");
    }

    History.addValueChangeHandler(this);

    History.fireCurrentHistoryState();
  }

    void setSessionID(String sessionID) {
        GoteFarm.sessionID = sessionID;

        // refresh UI elements that depend on the sessionID
        events.refresh();
        chars.loadCharacters();
        admin.refresh();
    }

    public void onValueChange(ValueChangeEvent<String> event) {
        final String historyToken = event.getValue();
        if (historyToken.equals("guilds")) {
            tabpanel.selectTab(0);
            events.resizeRows();
        }
        else if (historyToken.equals("events")) {
            tabpanel.selectTab(1);
            events.resizeRows();
        }
        else if (historyToken.equals("characters")) {
            tabpanel.selectTab(2);
        }
        else if (historyToken.equals("admin")) {
            tabpanel.selectTab(3);
        }
    }

    public void onSelection(SelectionEvent<Integer> event) {
        switch (event.getSelectedItem()) {
            case 0:
                History.newItem("guilds");
                break;
            case 1:
                History.newItem("events");
                break;
            case 2:
                History.newItem("characters");
                break;
            case 3:
                History.newItem("admin");
                break;
            default:
        }
    }
}
