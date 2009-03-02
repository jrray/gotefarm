package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Events extends Composite {

    VerticalPanel vpanel = new VerticalPanel();
    final DateTimeFormat time_formatter = DateTimeFormat.getFormat("EEE MMM dd, yyyy hh:mm aaa");

    public class Event extends Composite {

        static final String SPACE_AVAILABLE = "available";

        JSEvent event = null;
        JSEventSignups signups = null;

        VerticalPanel vpanel = new VerticalPanel();
        FlexTable flex = new FlexTable();

        public Event(JSEvent event) {
            this.event = event;

            vpanel.setWidth("100%");
            flex.setWidth("100%");

            vpanel.add(new Label(event.name));
            vpanel.add(new Label(time_formatter.format(event.start_time)));
            vpanel.add(flex);

            showSignups();
            fetchSignups();

            initWidget(vpanel);

            setStyleName("Event");
        }

        void styleColumn(FlexCellFormatter formatter, int column) {
            formatter.addStyleName(1, column, "coming");
            formatter.setVerticalAlignment(1, column, HasVerticalAlignment.ALIGN_TOP);
            formatter.addStyleName(2, column, "standby");
            formatter.setVerticalAlignment(2, column, HasVerticalAlignment.ALIGN_TOP);
            formatter.addStyleName(3, column, "maybe");
            formatter.setVerticalAlignment(3, column, HasVerticalAlignment.ALIGN_TOP);
            formatter.addStyleName(4, column, "notcoming");
            formatter.setVerticalAlignment(4, column, HasVerticalAlignment.ALIGN_TOP);
        }

        void showSignups() {
            flex.clear();

            class RoleSignup {
                int spaces_left;
                Set<JSEventBadge> badges_required = new HashSet<JSEventBadge>();
                Map<JSEventBadge, Integer> badges_needed = new HashMap<JSEventBadge, Integer>();
                List<JSEventSignup> coming = new ArrayList<JSEventSignup>();
                List<JSEventSignup> standby = new ArrayList<JSEventSignup>();
                List<JSEventSignup> maybe = new ArrayList<JSEventSignup>();
                List<JSEventSignup> not_coming = new ArrayList<JSEventSignup>();
            }

            List<JSEventSignup> limbo = new ArrayList<JSEventSignup>();
            Map<Long, RoleSignup> role_signups = new HashMap<Long, RoleSignup>();

            // For each role, figure out the badge requirements
            for (JSEventRole role : event.roles) {
                RoleSignup rsup = new RoleSignup();
                rsup.spaces_left = role.max;
                role_signups.put(role.roleid, rsup);

                for (JSEventBadge badge : event.badges) {
                    if (   badge.requireForSignup
                        && (   badge.applyToRole == null
                            || badge.applyToRole.equals(role.name))) {
                        if (badge.applyToRole == null) {
                            rsup.badges_required.add(badge);
                        }
                        else {
                            rsup.badges_needed.put(badge, badge.numSlots);
                        }
                    }
                }
            }

            int spots_left = event.size;

            if (signups != null) {
                // Categorize the current signups
                for (JSEventSignup es : signups.signups) {
                    RoleSignup rsup = role_signups.get(es.role.roleid);

                    // role does not exist: limbo
                    if (rsup == null) {
                        limbo.add(es);
                        continue;
                    }

                    // user not coming
                    if (es.signup_type == es.SIGNUP_TYPE_MAYBE) {
                        rsup.maybe.add(es);
                        continue;
                    }
                    else if (es.signup_type == es.SIGNUP_TYPE_NOT_COMING) {
                        rsup.not_coming.add(es);
                        continue;
                    }

                    // event full?
                    if (spots_left == 0) {
                        rsup.standby.add(es);
                        continue;
                    }

                    // role full?
                    if (rsup.spaces_left == 0) {
                        rsup.standby.add(es);
                        continue;
                    }

                    // character missing required badges?
                    boolean standby = false;
                    for (JSEventBadge req_badge : rsup.badges_required) {
                        if (!es.chr.hasBadge(req_badge.badgeid)) {
                            rsup.standby.add(es);
                            standby = true;
                            break;
                        }
                    }

                    // check for reserved spots
                    int role_spots_left = Math.min(spots_left, rsup.spaces_left);
                    for (Map.Entry<JSEventBadge, Integer> needed : rsup.badges_needed.entrySet()) {
                        if (needed.getValue() >= role_spots_left) {
                            // character must have this badge to sign up
                            if (!es.chr.hasBadge(needed.getKey().badgeid)) {
                                rsup.standby.add(es);
                                standby = true;
                                break;
                            }
                        }
                    }

                    if (standby) {
                        continue;
                    }

                    // this character is allowed in!
                    --spots_left;
                    --rsup.spaces_left;

                    // subtract out any badges requirements this character fulfills
                    List<JSEventBadge> to_delete = new ArrayList<JSEventBadge>();
                    for (Map.Entry<JSEventBadge, Integer> needed : rsup.badges_needed.entrySet()) {
                        int num = needed.getValue();
                        if (es.chr.hasBadge(needed.getKey().badgeid)) {
                            if (num == 1) {
                                to_delete.add(needed.getKey());
                            }
                            else {
                                needed.setValue(num - 1);
                            }
                        }
                    }

                    for (JSEventBadge badge : to_delete) {
                        rsup.badges_needed.remove(badge);
                    }

                    rsup.coming.add(es);
                }
            }

            FlexCellFormatter formatter = flex.getFlexCellFormatter();

            flex.setText(1, 0, JSEventSignup.SIGNUP_LABEL_COMING);
            flex.setText(2, 0, "Standby");
            flex.setText(3, 0, JSEventSignup.SIGNUP_LABEL_MAYBE);
            flex.setText(4, 0, JSEventSignup.SIGNUP_LABEL_NOT_COMING);

            styleColumn(formatter, 0);

            int column = 1;
            for (JSEventRole role : event.roles) {
                RoleSignup rsup = role_signups.get(role.roleid);

                flex.setText(0, column, role.name);

                styleColumn(formatter, column);

                VerticalPanel vsign = new VerticalPanel();
                vsign.setWidth("100%");

                // show signups
                for (JSEventSignup sup : rsup.coming) {
                    vsign.add(new Label(sup.chr.name));
                }

                // show remaining slots
                int eff_spots_left = Math.min(rsup.spaces_left, spots_left);
                for (int i = 0; i < eff_spots_left; ++i) {
                    HorizontalPanel hpanel = new HorizontalPanel();
                    hpanel.add(new Label(SPACE_AVAILABLE));

                    for (JSEventBadge badge : rsup.badges_required) {
                        Label l = new Label("!");
                        l.setTitle("Badge required: " + badge.name);
                        hpanel.add(l);
                    }

                    for (Map.Entry<JSEventBadge, Integer> needed : rsup.badges_needed.entrySet()) {
                        int num = needed.getValue();

                        if (num >= eff_spots_left) {
                            Label l = new Label("!");
                            l.setTitle("Badge required: " + needed.getKey().name);
                            hpanel.add(l);
                        }
                        // mark the last spots as reserved, they will switch to
                        // required as more people sign up who are missing this
                        // badge
                        else if (num >= eff_spots_left - i) {
                            Label l = new Label("*");
                            l.setTitle("Reserved for: " + needed.getKey().name);
                            hpanel.add(l);
                        }
                    }

                    vsign.add(hpanel);
                }

                flex.setWidget(1, column, vsign);

                // show standby
                VerticalPanel vstand = new VerticalPanel();
                vstand.setWidth("100%");

                for (JSEventSignup sup : rsup.standby) {
                    vstand.add(new Label(sup.chr.name));
                }

                flex.setWidget(2, column, vstand);

                // show maybe
                VerticalPanel vmaybe = new VerticalPanel();
                vmaybe.setWidth("100%");

                for (JSEventSignup sup : rsup.maybe) {
                    vmaybe.add(new Label(sup.chr.name));
                }

                flex.setWidget(3, column, vmaybe);

                // show not coming
                VerticalPanel vnotcoming = new VerticalPanel();
                vnotcoming.setWidth("100%");

                for (JSEventSignup sup : rsup.not_coming) {
                    vnotcoming.add(new Label(sup.chr.name));
                }

                flex.setWidget(4, column, vnotcoming);

                ++column;
            }

            if (!limbo.isEmpty()) {
                styleColumn(formatter, column);

                // create limbo column
                flex.setText(0, column, "Limbo");

                VerticalPanel vstand = new VerticalPanel();
                vstand.setWidth("100%");

                for (JSEventSignup sup : limbo) {
                    vstand.add(new Label(sup.chr.name));
                }

                flex.setWidget(2, column, vstand);
            }
        }

        void populateRole(JSEventRole role, int column) {
            flex.setText(0, column, role.name + " (" + role.min + "/" + role.max + ")");

            HashMap<String, Integer> role_badges = new HashMap<String, Integer>();

            // find the badge requirements
            for (JSEventBadge badge : event.badges) {
                if (   badge.requireForSignup
                    && (   badge.applyToRole == null
                        || badge.applyToRole.equals(role.name))) {
                    int num_slots;
                    if (badge.applyToRole == null) {
                        num_slots = role.max;
                    }
                    else {
                        num_slots = badge.numSlots;
                    }
                    role_badges.put(badge.name, num_slots);
                }
            }

            int spaces_left = role.max;
            for (int row = 1; row <= role.max; ++row) {

                StringBuilder sb = new StringBuilder(64).append(SPACE_AVAILABLE);

                for (Map.Entry<String, Integer> en : role_badges.entrySet()) {
                    int val = en.getValue();
                    if (val > 0) {
                        if (val >= spaces_left) {
                            sb.append(" - requires ")
                              .append(en.getKey());
                        }
                        else {
                            sb.append(" - reserved for ")
                              .append(en.getKey());
                        }

                        en.setValue(val - 1);
                    }
                }

                flex.setText(row, column, sb.toString());

                --spaces_left;
            }
        }

        void fetchSignups() {
            Date asof;
            if (signups != null) {
                asof = signups.asof;
            }
            else {
                asof = new Date(0L);
            }

            GoteFarm.goteService.getEventSignups(GoteFarm.sessionID, event.eid, asof, new AsyncCallback<JSEventSignups>() {
                public void onSuccess(JSEventSignups signups) {
                    if (signups == null) {
                        // unchanged
                        return;
                    }

                    Event.this.signups = signups;
                    showSignups();
                }

                public void onFailure(Throwable caught) {
                }
            });
        }
    }

    public Events() {
        vpanel.setWidth("100%");
        vpanel.setSpacing(20);

        vpanel.add(new Label("You are not signed in."));

        initWidget(vpanel);

        setStyleName("Characters");
    }

    public void refresh() {
        vpanel.clear();

        if (GoteFarm.sessionID == null) {
            vpanel.add(new Label("You are not signed in."));
            return;
        }

        GoteFarm.goteService.getEvents(GoteFarm.sessionID, new AsyncCallback<List<JSEvent>>() {
            public void onSuccess(List<JSEvent> result) {
                vpanel.clear();

                for (JSEvent e : result) {
                    vpanel.add(new Event(e));
                }

                if (result.size() == 0) {
                    vpanel.add(new Label("No events"));
                }
            }

            public void onFailure(Throwable caught) {
                vpanel.add(new Label(caught.getMessage()));
            }
        });
    }
}
