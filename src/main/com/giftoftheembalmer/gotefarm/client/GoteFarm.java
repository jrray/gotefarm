package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimpleCheckBox;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;

import java.util.Date;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class GoteFarm implements EntryPoint, ValueChangeHandler<String>, SelectionHandler<Integer> {

  static final String COOKIE_NAME = "gotefarm_sid";
  static GoteFarmRPCAsync goteService = null;
  static String sessionID = null;

  TabPanel tabpanel;
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
    events = new Events();
    chars = new Characters();
    admin = new Admin();

    // Events wants to know when there are changes to the user's characters
    chars.addValueChangeHandler(events);

    final VerticalPanel vpanel = new VerticalPanel();
    vpanel.setWidth("100%");

    String cookiesid = Cookies.getCookie(COOKIE_NAME);
    if (cookiesid != null) {
        goteService.validateSID(cookiesid, new AsyncCallback<String>() {
            public void onSuccess(String result) {
                if (result != null) {
                    setSessionID(result);
                    final RootPanel login = RootPanel.get("login");
                    login.setVisible(false);
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
    tabpanel.addSelectionHandler(this);

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

  void showLogin() {
        // login form widgets
        final TextBox username = TextBox.wrap(DOM.getElementById("login_username"));
        final PasswordTextBox password = PasswordTextBox.wrap(DOM.getElementById("login_password"));
        final SimpleCheckBox rememberme = SimpleCheckBox.wrap(DOM.getElementById("remember"));
        final Button submit = Button.wrap(DOM.getElementById("loginattempt"));
        final FormPanel loginForm = FormPanel.wrap(DOM.getElementById("loginform"), true);

        final RootPanel login = RootPanel.get("login");
        login.setVisible(true);

        submit.setEnabled(true);

        final Label errmsg = new Label();
        errmsg.addStyleName(errmsg.getStylePrimaryName() + "-error");
        login.add(errmsg);

        HorizontalPanel hpanel = new HorizontalPanel();
        hpanel.setWidth("100%");

        hpanel.add(new Hyperlink("Register", "register"));

        login.add(hpanel);

        loginForm.addSubmitHandler(new SubmitHandler() {
            public void onSubmit(SubmitEvent event) {
                errmsg.setVisible(false);

                String u = username.getText();

                if (u == null || u.length() == 0) {
                    errmsg.setText("You must provide a username");
                    errmsg.setVisible(true);
                    username.setFocus(true);
                    event.cancel();
                    return;
                }

                String pw = password.getText();

                if (pw == null || pw.length() == 0) {
                    errmsg.setText("You must provide a password");
                    errmsg.setVisible(true);
                    password.setFocus(true);
                    event.cancel();
                    return;
                }

                submit.setEnabled(false);
            }
        });
        loginForm.addSubmitCompleteHandler(new SubmitCompleteHandler() {
            public void onSubmitComplete(SubmitCompleteEvent event) {
                submit.setEnabled(true);

                String results = event.getResults();

                if (results.contains("NOK")) {
                    errmsg.setText("Invalid username or password.");
                    errmsg.setVisible(true);
                    return;
                }

                int ok = results.indexOf("OK,");
                if (ok < 0) {
                    errmsg.setText("Invalid username or password.");
                    errmsg.setVisible(true);
                    return;
                }

                String id = results.substring(ok+3, results.indexOf(",", ok+3));
                setSessionID(id);

                if (rememberme.isChecked()) {
                    final long DURATION = 1000 * 60 * 60 * 24 * 14; //duration remembering login. 2 weeks in this example.
                    Date expires = new Date(System.currentTimeMillis() + DURATION);
                    Cookies.setCookie(COOKIE_NAME, sessionID, expires, null, "/", false);
                }
                else {
                    Cookies.removeCookie(COOKIE_NAME);
                }

                login.setVisible(false);
            }
        });
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
            events.resizeRows();
        }
        else if (historyToken.equals("characters")) {
            tabpanel.selectTab(1);
        }
        else if (historyToken.equals("admin")) {
            tabpanel.selectTab(2);
        }
    }

    public void onSelection(SelectionEvent<Integer> event) {
        switch (event.getSelectedItem()) {
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
