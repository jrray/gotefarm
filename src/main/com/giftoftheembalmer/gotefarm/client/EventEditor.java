package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;

import java.util.ArrayList;
import java.util.List;

public class EventEditor extends Composite implements ChangeHandler {
    Admin admin;
    JSEventTemplate et;

    VerticalPanel vpanel = new VerticalPanel();
    TextBox name = new TextBox();
    TextBox size = new TextBox();
    TextBox minlevel = new TextBox();
    ListBox instances = new ListBox();
    TextBox newinst = new TextBox();
    ListBox bosses = new ListBox(true);
    TextBox newboss = new TextBox();
    ListBox roles = new ListBox();
    TextBox newrole = new TextBox();
    FlexTable roleft = new FlexTable();
    ListBox badges = new ListBox();
    TextBox newbadge = new TextBox();
    FlexTable badgeft = new FlexTable();

    final String NEW_EVENT = "New Event";
    final String NEW_INSTANCE = "New Instance";
    final String NEW_BOSS = "New Boss";
    final String NEW_ROLE = "New Role";
    final String NEW_BADGE = "New Badge";
    final String SELECT_A_ROLE = "Select a role";
    final String SELECT_A_BADGE = "Select a badge";
    final String ALL_ROLES = "All roles";
    final String MAX_LEVEL = "80";

    public EventEditor(final Admin admin, JSEventTemplate et) {
        this.admin = admin;
        this.et = et;

        vpanel.setWidth("100%");
        vpanel.setHeight("100%");

        final Label errmsg = new Label();
        errmsg.addStyleName(errmsg.getStylePrimaryName() + "-error");
        errmsg.addStyleName(errmsg.getStylePrimaryName() + "-bottom");

        FlexTable grid = new FlexTable();
        grid.setWidth("100%");

        CellFormatter cf = grid.getCellFormatter();
        // right align field labels
        cf.setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_RIGHT);
        cf.setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_RIGHT);
        cf.setHorizontalAlignment(2, 0, HasHorizontalAlignment.ALIGN_RIGHT);
        cf.setHorizontalAlignment(3, 0, HasHorizontalAlignment.ALIGN_RIGHT);
        cf.setHorizontalAlignment(4, 0, HasHorizontalAlignment.ALIGN_RIGHT);
        cf.setHorizontalAlignment(5, 0, HasHorizontalAlignment.ALIGN_RIGHT);
        cf.setHorizontalAlignment(7, 0, HasHorizontalAlignment.ALIGN_RIGHT);

        grid.setText(0, 0, "Event Name:");
        grid.setText(1, 0, "Raid Size:");
        grid.setText(2, 0, "Minimum Level:");
        grid.setText(3, 0, "Instance:");
        grid.setText(4, 0, "Bosses:");
        grid.setText(5, 0, "Roles:");
        grid.setText(7, 0, "Badges:");

        size.setVisibleLength(2);
        minlevel.setVisibleLength(2);

        size.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                try {
                    int sizen = Integer.parseInt(size.getText());
                    if (sizen < 0) {
                        size.setText("0");
                    }
                }
                catch (NumberFormatException e) {
                    size.setText("25");
                }

                updateRoleTotals();
            }
        });

        minlevel.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                try {
                    int minleveln = Integer.parseInt(minlevel.getText());
                    if (minleveln < 0) {
                        size.setText("0");
                    }
                }
                catch (NumberFormatException e) {
                    size.setText(MAX_LEVEL);
                }
            }
        });

        instances.setWidth("100%");
        instances.setVisibleItemCount(1);

        roles.setWidth("100%");
        roles.setVisibleItemCount(1);

        badges.setWidth("100%");
        badges.setVisibleItemCount(1);

        grid.setWidget(0, 1, name);
        grid.setWidget(1, 1, size);
        grid.setWidget(2, 1, minlevel);

        grid.setWidget(3, 1, instances);

        GoteFarm.goteService.getInstances(admin.current_guild.key,
                                          new AsyncCallback<List<JSInstance>>() {
            public void onSuccess(List<JSInstance> results) {
                int sel = 0;

                for (JSInstance i : results) {
                    instances.addItem(i.name, i.key);
                    if (EventEditor.this.et != null && i.equals(EventEditor.this.et.instance)) {
                        sel = instances.getItemCount() - 1;
                    }
                }

                instances.setSelectedIndex(sel);

                updateBosses();
            }

            public void onFailure(Throwable caught) {
            }
        });

        instances.addChangeHandler(this);
        roles.addChangeHandler(this);
        badges.addChangeHandler(this);

        grid.setWidget(3, 2, newinst);

        newinst.setText(NEW_INSTANCE);

        newinst.addKeyPressHandler(new KeyPressHandler() {
            public void onKeyPress(KeyPressEvent event) {
                if (event.getCharCode() == KeyCodes.KEY_ENTER) {
                    final String inst = newinst.getText();

                    boolean found = false;

                    for (int i = 0; i < instances.getItemCount(); ++i) {
                        if (instances.getItemText(i).equals(inst)) {
                            instances.setSelectedIndex(i);
                            focusBoss();
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        GoteFarm.goteService.addInstance(
                                            admin.current_guild.key,
                                            inst,
                                            new AsyncCallback<JSInstance>() {
                            public void onSuccess(JSInstance result) {
                                instances.addItem(result.name, result.key);
                                instances.setSelectedIndex(instances.getItemCount()-1);
                                bosses.clear();

                                focusBoss();
                            }

                            public void onFailure(Throwable caught) {
                                errmsg.setText(caught.getMessage());
                            }
                        });
                    }
                }
            }
        });

        grid.setWidget(4, 1, bosses);
        grid.setWidget(4, 2, newboss);

        bosses.setWidth("100%");
        bosses.setVisibleItemCount(10);

        newboss.setText(NEW_BOSS);

        newboss.addKeyPressHandler(new KeyPressHandler() {
            public void onKeyPress(KeyPressEvent event) {
                if (event.getCharCode() == KeyCodes.KEY_ENTER) {
                    int selinst = instances.getSelectedIndex();
                    if (selinst == -1) {
                        // TODO: display error
                        return;
                    }

                    String boss = newboss.getText();

                    boolean found = false;

                    for (int i = 0; i < bosses.getItemCount(); ++i) {
                        if (bosses.getItemText(i).equals(boss)) {
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        bosses.addItem(boss);
                        bosses.setItemSelected(bosses.getItemCount()-1, true);

                        GoteFarm.goteService.addBoss(
                            GoteFarm.sessionID,
                            instances.getItemText(selinst),
                            boss,
                            new AsyncCallback<Boolean>() {

                            public void onSuccess(Boolean result) {
                            }

                            public void onFailure(Throwable caught) {
                            }
                        });
                    }

                    newboss.setText(NEW_BOSS);
                    newboss.setSelectionRange(0, NEW_BOSS.length());
                }
            }
        });

        grid.setWidget(5, 1, roles);

        roles.addItem(SELECT_A_ROLE);

        GoteFarm.goteService.getRoles(new AsyncCallback<List<JSRole>>() {
            public void onSuccess(List<JSRole> results) {
                for (JSRole i : results) {
                    roles.addItem(i.name);
                }
            }

            public void onFailure(Throwable caught) {
            }
        });

        grid.setWidget(5, 2, newrole);

        newrole.setText(NEW_ROLE);

        newrole.addKeyPressHandler(new KeyPressHandler() {
            public void onKeyPress(KeyPressEvent event) {
                if (event.getCharCode() == KeyCodes.KEY_ENTER) {
                    String role = newrole.getText();

                    boolean found = false;

                    for (int i = 0; i < roles.getItemCount(); ++i) {
                        if (roles.getItemText(i).equals(role)) {
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        roles.addItem(role);
                        roles.setSelectedIndex(roles.getItemCount()-1);

                        newrole.setFocus(true);
                        newrole.setText(NEW_ROLE);
                        newrole.setSelectionRange(0, NEW_ROLE.length());

                        addRole(role);

                        GoteFarm.goteService.addRole(GoteFarm.sessionID, role, true, new AsyncCallback<Boolean>() {
                            public void onSuccess(Boolean result) {
                            }

                            public void onFailure(Throwable caught) {
                            }
                        });
                    }
                }
            }
        });

        roleft.setWidth("100%");
        roleft.setCellSpacing(0);
        roleft.setCellPadding(5);
        roleft.setText(0, 0, "Role");
        roleft.setText(0, 1, "Min");
        roleft.setText(0, 2, "Max");
        roleft.setText(1, 0, "Totals");

        FlexTable.FlexCellFormatter rcf = roleft.getFlexCellFormatter();
        rcf.addStyleName(0, 0, "header");
        rcf.addStyleName(0, 1, "header");
        rcf.addStyleName(0, 2, "header");
        rcf.addStyleName(0, 3, "header");
        rcf.addStyleName(1, 0, "footer");
        rcf.addStyleName(1, 1, "footer");
        rcf.addStyleName(1, 2, "footer");
        rcf.addStyleName(1, 3, "footer");

        FlexTable.FlexCellFormatter gcf = grid.getFlexCellFormatter();

        grid.setWidget(6, 0, roleft);
        gcf.setColSpan(6, 0, 3);

        grid.setWidget(7, 1, badges);

        badges.addItem(SELECT_A_BADGE);

        GoteFarm.goteService.getBadges(new AsyncCallback<List<JSBadge>>() {
            public void onSuccess(List<JSBadge> results) {
                for (JSBadge badge : results) {
                    badges.addItem(badge.name);
                }
            }

            public void onFailure(Throwable caught) {
            }
        });

        grid.setWidget(7, 2, newbadge);

        newbadge.setText(NEW_BADGE);

        newbadge.addKeyPressHandler(new KeyPressHandler() {
            public void onKeyPress(KeyPressEvent event) {
                if (event.getCharCode() == KeyCodes.KEY_ENTER) {
                    String badge = newbadge.getText();

                    boolean found = false;

                    for (int i = 0; i < badges.getItemCount(); ++i) {
                        if (badges.getItemText(i).equals(badge)) {
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        badges.addItem(badge);
                        badges.setSelectedIndex(badges.getItemCount()-1);

                        newbadge.setFocus(true);
                        newbadge.setText(NEW_BADGE);
                        newbadge.setSelectionRange(0, NEW_BADGE.length());

                        addBadge(badge);

                        GoteFarm.goteService.addBadge(GoteFarm.sessionID, badge, 0, new AsyncCallback<Boolean>() {
                            public void onSuccess(Boolean result) {
                            }

                            public void onFailure(Throwable caught) {
                            }
                        });
                    }
                }
            }
        });

        badgeft.setWidth("100%");
        badgeft.setCellSpacing(0);
        badgeft.setCellPadding(5);
        badgeft.setText(0, 0, "Badge");
        badgeft.setText(0, 1, "Require For Sign Up");
        badgeft.setText(0, 2, "Apply To Role");
        badgeft.setText(0, 3, "Num Role Slots");
        badgeft.setText(0, 4, "Early Signup (Hours)");

        FlexTable.FlexCellFormatter bcf = badgeft.getFlexCellFormatter();
        bcf.addStyleName(0, 0, "header");
        bcf.addStyleName(0, 1, "header");
        bcf.addStyleName(0, 2, "header");
        bcf.addStyleName(0, 3, "header");
        bcf.addStyleName(0, 4, "header");
        bcf.addStyleName(0, 5, "header");

        grid.setWidget(8, 0, badgeft);
        gcf.setColSpan(8, 0, 3);

        vpanel.add(grid);

        HorizontalPanel hpanel = new HorizontalPanel();
        hpanel.setWidth("100%");

        final CheckBox modify = new CheckBox("Modify published events (can change signups)");
        modify.setValue(true);
        modify.addStyleName(modify.getStylePrimaryName() + "-bottom");
        modify.addStyleName(modify.getStylePrimaryName() + "-left");

        Button save = new Button("Save", new ClickHandler() {
            public void onClick(ClickEvent event) {
                // clear error message
                errmsg.setText("");

                JSEventTemplate t = new JSEventTemplate();

                if (EventEditor.this.et != null) {
                    t.eid = EventEditor.this.et.eid;
                }
                else {
                    t.eid = -1;
                }
                t.name = name.getText();
                t.size = getRaidSize();
                t.minimumLevel = Integer.parseInt(minlevel.getText());
                int index = instances.getSelectedIndex();
                if (index < 0) {
                    errmsg.setText("Please select an instance for this event.");
                    return;
                }
                t.instance = instances.getItemText(index);

                t.bosses = new ArrayList<String>();
                for (int i = 0; i < bosses.getItemCount(); ++i) {
                    if (bosses.isItemSelected(i)) {
                        t.bosses.add(bosses.getItemText(i));
                    }
                }

                t.roles = new ArrayList<JSEventRole>();
                for (int i = 0; i < roleft.getRowCount() - 2; ++i) {
                    JSEventRole er = new JSEventRole();
                    er.name = roleft.getText(i + 1, 0);
                    TextBox minmax;
                    minmax = (TextBox)roleft.getWidget(i + 1, 1);
                    er.min = Integer.parseInt(minmax.getText());
                    minmax = (TextBox)roleft.getWidget(i + 1, 2);
                    er.max = Integer.parseInt(minmax.getText());
                    t.roles.add(er);
                }

                t.badges = new ArrayList<JSEventBadge>();
                for (int i = 0; i < badgeft.getRowCount() - 1; ++i) {
                    JSEventBadge eb = new JSEventBadge();
                    eb.name = badgeft.getText(i + 1, 0);
                    CheckBox cb = (CheckBox)badgeft.getWidget(i + 1, 1);
                    eb.requireForSignup = cb.getValue();
                    ListBox lb = (ListBox)badgeft.getWidget(i + 1, 2);
                    String role = lb.getItemText(lb.getSelectedIndex());
                    if (!role.equals(ALL_ROLES)) {
                        eb.applyToRole = role;
                    }
                    TextBox tb;
                    tb = (TextBox)badgeft.getWidget(i + 1, 3);
                    eb.numSlots = Integer.parseInt(tb.getText());
                    tb = (TextBox)badgeft.getWidget(i + 1, 4);
                    eb.earlySignup = Integer.parseInt(tb.getText());
                    t.badges.add(eb);
                }

                t.modifyEvents = modify.getValue();

                GoteFarm.goteService.saveEventTemplate(GoteFarm.sessionID, t, new AsyncCallback<Boolean>() {
                    public void onSuccess(Boolean result) {
                        EventEditor.this.admin.eventAdded();
                        EventEditor.this.admin.setCenterWidget(null);
                    }

                    public void onFailure(Throwable caught) {
                        errmsg.setText(caught.getMessage());
                    }
                });
            }
        });

        save.addStyleName(save.getStylePrimaryName() + "-bottom");
        save.addStyleName(save.getStylePrimaryName() + "-left");

        Button cancel = new Button("Cancel", new ClickHandler() {
            public void onClick(ClickEvent event) {
                EventEditor.this.admin.setCenterWidget(null);
            }
        });

        cancel.addStyleName(cancel.getStylePrimaryName() + "-bottom");
        cancel.addStyleName(cancel.getStylePrimaryName() + "-right");

        hpanel.add(save);
        // Editing an existing event?
        if (EventEditor.this.et != null) {
            hpanel.add(modify);
        }
        hpanel.add(errmsg);
        hpanel.add(cancel);

        vpanel.add(hpanel);

        if (et != null) {
            for (JSEventRole ev : et.roles) {
                addRole(ev.name, ev.min, ev.max);
            }

            for (JSEventBadge eb : et.badges) {
                addBadge(eb.name, eb.requireForSignup, eb.applyToRole, eb.numSlots, eb.earlySignup);
            }
        }

        if (et == null) {
            name.setText(NEW_EVENT);

            DeferredCommand.addCommand(new Command() {
                public void execute() {
                    name.setFocus(true);
                    name.setSelectionRange(0, NEW_EVENT.length());
                }
            });

            size.setText("25");
            minlevel.setText(MAX_LEVEL);
        }
        else {
            name.setText(et.name);
            size.setText("" + et.size);
            minlevel.setText("" + et.minimumLevel);
        }

        updateRoleTotals();

        initWidget(vpanel);

        setStyleName("Admin-EventEditor");
    }

    public EventEditor(Admin admin) {
        this(admin, null);
    }

    private void focusBoss() {
        newboss.setFocus(true);
        newboss.setText(NEW_BOSS);
        newboss.setSelectionRange(0, NEW_BOSS.length());
    }

    public void updateBosses() {
        int index = instances.getSelectedIndex();
        if (index < 0) return;

        final String inst_key = instances.getValue(index);

        GoteFarm.goteService.getInstanceBosses(
                                            inst_key,
                                            new AsyncCallback<List<JSBoss>>() {
            public void onSuccess(List<JSBoss> results) {
                bosses.clear();

                for (JSBoss ib : results) {
                    bosses.addItem(ib.name, ib.key);
                    if (et != null && inst_key.equals(et.instance)) {
                        for (String eb : et.bosses) {
                            if (ib.equals(eb)) {
                                bosses.setItemSelected(bosses.getItemCount() - 1, true);
                                break;
                            }
                        }
                    }
                }
            }

            public void onFailure(Throwable caught) {
            }
        });
    }

    public void addRole(String role) {
        addRole(role, 0, 0);
    }

    public void addRole(String role, int min_value, int max_value) {
        if (role.equals(SELECT_A_ROLE)) {
            return;
        }

        final int rows = roleft.getRowCount() - 2;

        // check role doesn't already exist
        for (int row = 0; row < rows; ++row) {
            if (roleft.getText(row + 1, 0).equals(role)) {
                return;
            }
        }

        roleft.insertRow(rows + 1);
        roleft.setText(rows + 1, 0, role);

        final TextBox min = new TextBox();
        final TextBox max = new TextBox();

        min.setText("" + min_value);
        max.setText("" + max_value);

        min.setVisibleLength(2);
        max.setVisibleLength(2);

        ChangeHandler minmax = new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                final Object sender = event.getSource();
                try {
                    Integer minv = Integer.parseInt(min.getText());
                    Integer maxv = Integer.parseInt(max.getText());

                    if (sender == min) {
                        if (minv > maxv) {
                            max.setText(min.getText());
                        }
                    }
                    else if (sender == max) {
                        if (maxv < minv) {
                            min.setText(max.getText());
                        }
                    }
                }
                catch (NumberFormatException e) {
                    if (sender == min) {
                        min.setText("0");
                    }
                    else if (sender == max) {
                        max.setText("0");
                    }
                }

                updateRoleTotals();
            }
        };

        min.addChangeHandler(minmax);
        max.addChangeHandler(minmax);

        roleft.setWidget(rows + 1, 1, min);
        roleft.setWidget(rows + 1, 2, max);
        roleft.setWidget(rows + 1, 3, new Button("Remove", new ClickHandler() {
            public void onClick(ClickEvent event) {
                final Object sender = event.getSource();
                final int rows = roleft.getRowCount() - 2;
                for (int i = 0; i < rows; ++i) {
                    if (roleft.getWidget(i + 1, 3) == sender) {
                        roleft.removeRow(i + 1);
                        updateBadgeRoles();
                        break;
                    }
                }
            }
        }));

        updateRoleTotals();
    }

    public void addBadge(String badge) {
        addBadge(badge, false, null, 0, 0);
    }

    public void addBadge(String badge, boolean requireForSignup, String applyToRole, int numSlots, int earlySignup) {
        if (badge.equals(SELECT_A_BADGE)) {
            return;
        }

        final int rows = badgeft.getRowCount() - 1;

        badgeft.setText(rows + 1, 0, badge);

        final CheckBox cb = new CheckBox();
        cb.setValue(requireForSignup);
        badgeft.setWidget(rows + 1, 1, cb);

        final ListBox roles = new ListBox();
        roles.setVisibleItemCount(1);

        roles.addItem(ALL_ROLES);

        int sel_index = 0;

        for (int i = 0; i < roleft.getRowCount() - 2; ++i) {
            String role = roleft.getText(i + 1, 0);
            roles.addItem(role);
            if (applyToRole != null && role.equals(applyToRole)) {
                sel_index = roles.getItemCount() - 1;
            }
        }

        roles.setSelectedIndex(sel_index);
        badgeft.setWidget(rows + 1, 2, roles);

        final TextBox slots = new TextBox();
        slots.setVisibleLength(2);
        if (requireForSignup && applyToRole != null) {
            slots.setText("" + numSlots);
        }
        else {
            slots.setEnabled(false);
            slots.setText("0");
        }
        badgeft.setWidget(rows + 1, 3, slots);
        slots.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                try {
                    int num = Integer.parseInt(slots.getText());
                    if (num < 0) {
                        slots.setText("0");
                    }
                }
                catch (NumberFormatException e) {
                    slots.setText("0");
                }
            }
        });

        cb.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                if (cb.getValue() && roles.getSelectedIndex() > 0) {
                    slots.setEnabled(true);
                }
                else {
                    slots.setEnabled(false);
                    slots.setText("0");
                }
            }
        });

        roles.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                if (cb.getValue() && roles.getSelectedIndex() > 0) {
                    slots.setEnabled(true);
                }
                else {
                    slots.setEnabled(false);
                    slots.setText("0");
                }
            }
        });

        final TextBox early = new TextBox();
        early.setVisibleLength(2);
        early.setText("" + earlySignup);
        badgeft.setWidget(rows + 1, 4, early);
        early.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                try {
                    int num = Integer.parseInt(early.getText());
                    if (num < 0) {
                        early.setText("0");
                    }
                }
                catch (NumberFormatException e) {
                    early.setText("0");
                }
            }
        });

        badgeft.setWidget(rows + 1, 5, new Button("Remove", new ClickHandler() {
            public void onClick(ClickEvent event) {
                final Object sender = event.getSource();
                final int rows = badgeft.getRowCount() - 1;
                for (int i = 0; i < rows; ++i) {
                    if (badgeft.getWidget(i + 1, 5) == sender) {
                        badgeft.removeRow(i + 1);
                        break;
                    }
                }
            }
        }));
    }

    public int getRaidSize() {
        try {
            return Integer.parseInt(size.getText());
        }
        catch (NumberFormatException e) {
            return 0;
        }
    }

    public void updateRoleTotals() {
        int mint = 0;
        int maxt = 0;

        final int rows = roleft.getRowCount() - 2;

        for (int row = 0; row < rows; ++row) {
            TextBox minw = (TextBox)roleft.getWidget(row + 1, 1);
            TextBox maxw = (TextBox)roleft.getWidget(row + 1, 2);

            mint += Integer.parseInt(minw.getText());
            maxt += Integer.parseInt(maxw.getText());
        }

        roleft.setText(rows + 1, 1, "" + mint);
        roleft.setText(rows + 1, 2, "" + maxt);

        int size = getRaidSize();

        FlexTable.FlexCellFormatter fcf = roleft.getFlexCellFormatter();

        if (mint < size) {
            fcf.addStyleName(rows + 1, 1, "error");
        }
        else {
            fcf.removeStyleName(rows + 1, 1, "error");
        }

        if (maxt < size) {
            fcf.addStyleName(rows + 1, 2, "error");
        }
        else {
            fcf.removeStyleName(rows + 1, 2, "error");
        }

        updateBadgeRoles();
    }

    public void updateBadgeRoles() {
        final int role_rows = roleft.getRowCount() - 2;
        final int badge_rows = badgeft.getRowCount() - 1;

        for (int i = 0; i < badge_rows; ++i) {
            ListBox broles = (ListBox)badgeft.getWidget(i + 1, 2);
            String selected = broles.getItemText(broles.getSelectedIndex());

            broles.clear();

            broles.addItem(ALL_ROLES);

            int selected_index = 0;

            for (int j = 0; j < role_rows; ++j) {
                String role = roleft.getText(j + 1, 0);
                broles.addItem(role);
                if (role.equals(selected)) {
                    selected_index = j + 1;
                }
            }

            broles.setSelectedIndex(selected_index);

            // should num slots be enabled?
            boolean enable = false;

            if (selected_index > 0) {
                CheckBox cb = (CheckBox)badgeft.getWidget(i + 1, 1);
                if (cb.getValue()) {
                    enable = true;
                }
            }

            TextBox tb = (TextBox)badgeft.getWidget(i + 1, 3);
            tb.setEnabled(enable);
            if (!enable) {
                tb.setText("0");
            }
        }
    }

    public void onChange(ChangeEvent event) {
        Object sender = event.getSource();

        if (sender == instances) {
            updateBosses();
        }
        else if (sender == roles) {
            addRole(roles.getItemText(roles.getSelectedIndex()));
        }
        else if (sender == badges) {
            addBadge(badges.getItemText(badges.getSelectedIndex()));
        }
    }
}
