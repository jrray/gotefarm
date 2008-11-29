package com.giftoftheembalmer.gotefarm.client;

import com.giftoftheembalmer.gotefarm.client.InvalidCredentialsError;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.HistoryListener;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SourcesTabEvents;
import com.google.gwt.user.client.ui.TabListener;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

import java.util.Date;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class GoteFarm implements EntryPoint, HistoryListener, TabListener {

  static final String COOKIE_NAME = "gotefarm_sid";
  static GoteFarmRPCAsync goteService = null;
  static String sessionID = null;

  TabPanel tabpanel;
  Characters chars;
  Admin admin;

  /**
   * This is the entry point method.
   */
  public void onModuleLoad() {
    goteService = (GoteFarmRPCAsync)GWT.create(GoteFarmRPC.class);

    ServiceDefTarget endpoint = (ServiceDefTarget)goteService;
    String moduleRelativeURL = GWT.getModuleBaseURL() + "servlet/goteService";
    endpoint.setServiceEntryPoint(moduleRelativeURL);

    tabpanel = new TabPanel();
    chars = new Characters();
    admin = new Admin();

    final RootPanel login = RootPanel.get("login");
    login.setVisible(false);

    final VerticalPanel vpanel = new VerticalPanel();
    vpanel.setWidth("100%");

    String cookiesid = Cookies.getCookie(COOKIE_NAME);
    if (cookiesid != null) {
        goteService.validateSID(cookiesid, new AsyncCallback<String>() {
            public void onSuccess(String result) {
                if (result != null) {
                    setSessionID(result);
                }
                else {
                    showLogin();
                }
            }

            public void onFailure(Throwable caught) {
                showLogin();
            }
        });
    }
    else {
        showLogin();
    }

    tabpanel.setWidth("98%");
    tabpanel.addStyleName(tabpanel.getStylePrimaryName() + "-main");
    tabpanel.addTabListener(this);

    tabpanel.add(new Label("Events"), "Events");
    tabpanel.add(chars, "Characters");
    tabpanel.add(admin, "Admin");

    vpanel.add(tabpanel);

    RootPanel.get().add(vpanel);

    String initToken = History.getToken();
    if (initToken.length() == 0) {
        History.newItem("events");
    }

    History.addHistoryListener(this);

    History.fireCurrentHistoryState();
  }

  void showLogin() {
        final RootPanel login = RootPanel.get("login");
        login.setVisible(true);

        final CheckBox rememberme = new CheckBox("Remember me on this computer");
        final Label errmsg = new Label();
        errmsg.addStyleName(errmsg.getStylePrimaryName() + "-error");
        login.add(rememberme);
        login.add(errmsg);

        HorizontalPanel hpanel = new HorizontalPanel();
        hpanel.setWidth("100%");

        hpanel.add(new Button("Login", new ClickListener() {
            String username = null;
            String password = null;

            public void onClick(Widget sender) {
                errmsg.setVisible(false);

                Element elem = DOM.getElementById("login_username");
                if (elem != null) {
                    username = DOM.getElementProperty(elem, "value");
                }

                elem = DOM.getElementById("login_password");
                if (elem != null) {
                    password = DOM.getElementProperty(elem, "value");
                }

                if (username != null && password != null) {
                    goteService.login(username, password, new AsyncCallback<String>() {
                        public void onSuccess(String result) {
                            setSessionID(result);
                            login.setVisible(false);

                            if (rememberme.isChecked()) {
                                final long DURATION = 1000 * 60 * 60 * 24 * 14; //duration remembering login. 2 weeks in this example.
                                Date expires = new Date(System.currentTimeMillis() + DURATION);
                                Cookies.setCookie(COOKIE_NAME, sessionID, expires, null, "/", false);
                            }
                            else {
                                Cookies.removeCookie(COOKIE_NAME);
                            }
                        }

                        public void onFailure(Throwable caught) {
                            try {
                                throw caught;
                            }
                            catch (InvalidCredentialsError e) {
                                errmsg.setText("Invalid username or password.");
                                errmsg.setVisible(true);
                            }
                            catch (Throwable e) {
                                errmsg.setText(e.toString());
                                errmsg.setVisible(true);
                            }
                        }
                    });
                }
                else {
                    Window.alert("Failed to locate username and password inputs");
                }
            }
        }));

        hpanel.add(new Hyperlink("Register", "register"));

        login.add(hpanel);
    }

    void setSessionID(String sessionID) {
        this.sessionID = sessionID;

        // refresh UI elements that depend on the sessionID
        chars.loadCharacters();
        admin.refresh();
    }

    public void onHistoryChanged(String historyToken) {
        if (historyToken.equals("register")) {
            RootPanel.get("login").setVisible(false);
            final RegisterPanel popup = new RegisterPanel(this);
            popup.setPopupPositionAndShow(new RegisterPanel.PositionCallback() {
            public void setPosition(int offsetWidth, int offsetHeight) {
                int left = (Window.getClientWidth() - offsetWidth) / 3;
                int top = (Window.getClientHeight() - offsetHeight) / 3;
                popup.setPopupPosition(left, top);
            }
            });
        }
        else if (historyToken.equals("events")) {
            tabpanel.selectTab(0);
        }
        else if (historyToken.equals("characters")) {
            tabpanel.selectTab(1);
        }
        else if (historyToken.equals("admin")) {
            tabpanel.selectTab(2);
        }
    }

    public boolean onBeforeTabSelected(SourcesTabEvents sender, int tabIndex) {
        return true;
    }

    public void onTabSelected(SourcesTabEvents sender, int tabIndex) {
        switch (tabIndex) {
            case 0:
                History.newItem("events");
                break;
            case 1:
                History.newItem("characters");
                break;
            case 2:
                History.newItem("admin");
                break;
            default:
        }
    }
}
