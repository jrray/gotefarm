package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;

import java.util.Date;
import java.util.List;

public class ScheduleEditor extends Composite {
    static int last_group_num = 0;

    class Schedule extends Composite implements ClickHandler, ValueChangeHandler<Date> {
        JSEventSchedule sched;

        // append a distinct number to radio button group names, per schedule,
        // so multiple schedules' radio buttons don't end up in the same group
        final int radio_group_num = last_group_num++;
        final String REPEAT_GROUP = "repeatGroup_" + radio_group_num;
        final String DAYOF_GROUP = "dayofGroup_" + radio_group_num;

        VerticalPanel vpanel = new VerticalPanel();
        final DateBox db;
        final TimePicker tp;
        final DurationPicker display_start = new DurationPicker();
        final DurationPicker display_end = new DurationPicker();
        final DurationPicker signups_start = new DurationPicker();
        final DurationPicker signups_end = new DurationPicker();
        final Label display_start_time = new Label();
        final Label display_end_time = new Label();
        final Label signups_start_time = new Label();
        final Label signups_end_time = new Label();
        final DateTimeFormat time_formatter = DateTimeFormat.getShortDateTimeFormat();
        final RadioButton rptnever = new RadioButton(REPEAT_GROUP, "Never");
        final RadioButton rptdaily = new RadioButton(REPEAT_GROUP, "Daily");
        final RadioButton rptweekly = new RadioButton(REPEAT_GROUP, "Weekly");
        final RadioButton rptmonthly = new RadioButton(REPEAT_GROUP, "Monthly");
        final VerticalPanel dailyrptpanel = new VerticalPanel();
        final VerticalPanel weeklyrptpanel = new VerticalPanel();
        final VerticalPanel monthlyrptpanel = new VerticalPanel();
        final ListBox dailyrptdays = new ListBox();
        final Label dailyrptdaylabel = new Label("day");
        final ListBox weeklyrptweeks = new ListBox();
        final Label weeklyrptweeklabel = new Label("week");
        final CheckBox[] weeklydays = {
            new CheckBox("Sun"),
            new CheckBox("Mon"),
            new CheckBox("Tue"),
            new CheckBox("Wed"),
            new CheckBox("Thu"),
            new CheckBox("Fri"),
            new CheckBox("Sat")
        };
        final ListBox monthlyrptmonth = new ListBox();
        final Label monthlyrptmonthlabel = new Label("month");
        final RadioButton rptdayofmonth = new RadioButton(DAYOF_GROUP, "day of the month");
        final RadioButton rptdayofweek = new RadioButton(DAYOF_GROUP, "day of the week");

        class DurationChangeHandler implements ValueChangeHandler<Integer> {

            public void onValueChange(ValueChangeEvent<Integer> event) {
                final Object sender = event.getSource();
                if (sched == null) return;

                if (sender == display_start) {
                    sched.display_start = display_start.getValue();
                }
                else if (sender == display_end) {
                    sched.display_end = display_end.getValue();
                }
                else if (sender == signups_start) {
                    sched.signups_start = signups_start.getValue();
                }
                else if (sender == signups_end) {
                    sched.signups_end = signups_end.getValue();
                }

                updateTimes();
            }
        }
        final DurationChangeHandler dch = new DurationChangeHandler();

        @SuppressWarnings("deprecation")
        public Schedule(String eventKey, JSEventSchedule sched) {
            this.sched = sched;

            if (this.sched == null) {
                this.sched = sched = new JSEventSchedule();

                sched.event_schedule_key = null;
                sched.event_template_key = eventKey;

                sched.start_time = new Date();
                sched.start_time.setTime(
                    sched.start_time.getTime() -
                    (sched.start_time.getTime() % 3600000)
                );
                sched.orig_start_time = sched.start_time;

                sched.duration = 120 * 60;

                sched.display_start = 7 * 86400;
                sched.display_end = 3600;

                sched.signups_start = 2 * 86400;
                sched.signups_end = 1800;

                sched.repeat_size = JSEventSchedule.REPEAT_NEVER;
                sched.repeat_freq = 1;
                sched.day_mask = 1 << sched.start_time.getDay();
                sched.repeat_by = JSEventSchedule.REPEAT_BY_DAY_OF_MONTH;

                sched.active = true;
            }

            vpanel.setWidth("100%");

            db = new DateBox();
            db.setValue(sched.start_time);
            db.setFormat(
                new DateBox.DefaultFormat(DateTimeFormat.getShortDateFormat())
            );
            db.addValueChangeHandler(this);
            tp = new TimePicker(sched.start_time);
            tp.addValueChangeHandler(this);

            {
                HorizontalPanel hpanel = new HorizontalPanel();
                hpanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
                hpanel.add(new Label("Start Date:"));
                hpanel.add(db);
                hpanel.add(new HTML("&nbsp;&nbsp;"));
                hpanel.add(new Label("Start Time:"));
                hpanel.add(tp);

                vpanel.add(hpanel);
            }

            display_start.addValueChangeHandler(dch);
            display_start.setVisibleLength(6);
            display_start.setValue(sched.display_start);

            {
                HorizontalPanel hpanel = new HorizontalPanel();
                hpanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
                hpanel.setSpacing(5);
                hpanel.add(new Label("Display event "));
                hpanel.add(display_start);
                hpanel.add(new Label(" before start time: "));
                hpanel.add(display_start_time);

                vpanel.add(hpanel);
            }

            display_end.addValueChangeHandler(dch);
            display_end.setVisibleLength(6);
            display_end.setValue(sched.display_end);

            {
                HorizontalPanel hpanel = new HorizontalPanel();
                hpanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
                hpanel.setSpacing(5);
                hpanel.add(new Label("Remove event "));
                hpanel.add(display_end);
                hpanel.add(new Label(" after start time: "));
                hpanel.add(display_end_time);

                vpanel.add(hpanel);
            }

            signups_start.addValueChangeHandler(dch);
            signups_start.setVisibleLength(6);
            signups_start.setValue(sched.signups_start);

            {
                HorizontalPanel hpanel = new HorizontalPanel();
                hpanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
                hpanel.setSpacing(5);
                hpanel.add(new Label("Signups open "));
                hpanel.add(signups_start);
                hpanel.add(new Label(" before start time: "));
                hpanel.add(signups_start_time);

                vpanel.add(hpanel);
            }

            signups_end.addValueChangeHandler(dch);
            signups_end.setVisibleLength(6);
            signups_end.setValue(sched.signups_end);

            {
                HorizontalPanel hpanel = new HorizontalPanel();
                hpanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
                hpanel.setSpacing(5);
                hpanel.add(new Label("Signups end "));
                hpanel.add(signups_end);
                hpanel.add(new Label(" before start time: "));
                hpanel.add(signups_end_time);

                vpanel.add(hpanel);
            }

            {
                HorizontalPanel rptpanel = new HorizontalPanel();

                rptpanel.add(new Label("Repeat event"));
                rptpanel.add(rptnever);
                rptpanel.add(rptdaily);
                rptpanel.add(rptweekly);
                rptpanel.add(rptmonthly);

                rptnever.addClickHandler(this);
                rptdaily.addClickHandler(this);
                rptweekly.addClickHandler(this);
                rptmonthly.addClickHandler(this);

                vpanel.add(rptpanel);
            }

            dailyrptpanel.setVisible(false);
            weeklyrptpanel.setVisible(false);
            monthlyrptpanel.setVisible(false);

            {
                HorizontalPanel hpanel = new HorizontalPanel();
                hpanel.add(new Label("Repeat every"));

                for (int i = 1; i < 31; ++i) {
                    dailyrptdays.addItem("" + i);
                }

                dailyrptdays.addChangeHandler(new ChangeHandler() {
                    public void onChange(ChangeEvent event) {
                        final int index = dailyrptdays.getSelectedIndex();

                        Schedule.this.sched.repeat_freq = index + 1;

                        if (index > 0) {
                            dailyrptdaylabel.setText("days");
                        }
                        else {
                            dailyrptdaylabel.setText("day");
                        }
                    }
                });

                dailyrptdays.setSelectedIndex(sched.repeat_freq - 1);

                hpanel.add(dailyrptdays);
                hpanel.add(dailyrptdaylabel);

                dailyrptpanel.add(hpanel);
                vpanel.add(dailyrptpanel);
            }

            {
                HorizontalPanel hpanel = new HorizontalPanel();
                hpanel.add(new Label("Repeat every"));

                for (int i = 1; i < 31; ++i) {
                    weeklyrptweeks.addItem("" + i);
                }

                weeklyrptweeks.addChangeHandler(new ChangeHandler() {
                    public void onChange(ChangeEvent event) {
                        final int index = weeklyrptweeks.getSelectedIndex();

                        Schedule.this.sched.repeat_freq = index + 1;

                        if (index > 0) {
                            weeklyrptweeklabel.setText("weeks");
                        }
                        else {
                            weeklyrptweeklabel.setText("week");
                        }
                    }
                });

                weeklyrptweeks.setSelectedIndex(sched.repeat_freq - 1);

                hpanel.add(weeklyrptweeks);
                hpanel.add(weeklyrptweeklabel);

                weeklyrptpanel.add(hpanel);
            }

            {
                HorizontalPanel hpanel = new HorizontalPanel();
                hpanel.add(new Label("Repeat on"));

                final ValueChangeHandler<Boolean> daychanged = new ValueChangeHandler<Boolean>() {
                    public void onValueChange(ValueChangeEvent<Boolean> event) {
                        final Object sender = event.getSource();
                        final boolean checked = event.getValue();
                        for (int i = 0; i < 7; ++i ) {
                            if (sender == weeklydays[i]) {
                                if (checked) {
                                    Schedule.this.sched.day_mask |= (1 << i);
                                }
                                else {
                                    Schedule.this.sched.day_mask &= ~(1 << i);
                                }
                                break;
                            }
                        }
                    }
                };

                for (int i = 0; i < 7; ++i ) {
                    weeklydays[i].addStyleName("padleft");

                    if ((sched.day_mask & (1 << i)) > 0) {
                        weeklydays[i].setValue(true);
                    }

                    weeklydays[i].addValueChangeHandler(daychanged);

                    hpanel.add(weeklydays[i]);
                }

                weeklyrptpanel.add(hpanel);
                vpanel.add(weeklyrptpanel);
            }

            {
                HorizontalPanel hpanel = new HorizontalPanel();
                hpanel.add(new Label("Repeat every"));

                for (int i = 1; i < 31; ++i) {
                    monthlyrptmonth.addItem("" + i);
                }

                monthlyrptmonth.addChangeHandler(new ChangeHandler() {
                    public void onChange(ChangeEvent event) {
                        final int index = monthlyrptmonth.getSelectedIndex();

                        Schedule.this.sched.repeat_freq = index + 1;

                        if (index > 0) {
                            monthlyrptmonthlabel.setText("months");
                        }
                        else {
                            monthlyrptmonthlabel.setText("month");
                        }
                    }
                });

                monthlyrptmonth.setSelectedIndex(sched.repeat_freq - 1);

                hpanel.add(monthlyrptmonth);
                hpanel.add(monthlyrptmonthlabel);

                monthlyrptpanel.add(hpanel);
            }

            {
                HorizontalPanel hpanel = new HorizontalPanel();
                hpanel.add(new Label("Repeat by"));

                final ValueChangeHandler<Boolean> rptdayofchanged = new ValueChangeHandler<Boolean>() {
                    public void onValueChange(ValueChangeEvent<Boolean> event) {
                        final Object sender = event.getSource();
                        if (sender == rptdayofmonth && event.getValue()) {
                            Schedule.this.sched.repeat_by = JSEventSchedule.REPEAT_BY_DAY_OF_MONTH;
                        }
                        else if (sender == rptdayofweek && event.getValue()) {
                            Schedule.this.sched.repeat_by = JSEventSchedule.REPEAT_BY_DAY_OF_WEEK;
                        }
                    }
                };

                if (sched.repeat_by == JSEventSchedule.REPEAT_BY_DAY_OF_MONTH) {
                    rptdayofmonth.setValue(true);
                }
                else {
                    rptdayofweek.setValue(true);
                }

                rptdayofmonth.addValueChangeHandler(rptdayofchanged);
                rptdayofweek.addValueChangeHandler(rptdayofchanged);

                hpanel.add(rptdayofmonth);
                hpanel.add(rptdayofweek);

                monthlyrptpanel.add(hpanel);
                vpanel.add(monthlyrptpanel);
            }

            updateTimes();

            switch (sched.repeat_size) {
                case JSEventSchedule.REPEAT_NEVER:
                    rptnever.setValue(true);
                    break;
                case JSEventSchedule.REPEAT_DAILY:
                    rptdaily.setValue(true);
                    onClick(rptdaily);
                    break;
                case JSEventSchedule.REPEAT_WEEKLY:
                    rptweekly.setValue(true);
                    onClick(rptweekly);
                    break;
                case JSEventSchedule.REPEAT_MONTHLY:
                    rptmonthly.setValue(true);
                    onClick(rptmonthly);
                    break;
            }

            HorizontalPanel hpanel = new HorizontalPanel();
            hpanel.setWidth("100%");

            final Label errmsg = new Label();
            errmsg.addStyleName(errmsg.getStylePrimaryName() + "-bottom");

            Button save = new Button("Save", new ClickHandler() {
                public void onClick(ClickEvent event) {
                    // clear error message
                    errmsg.setText("");

                    // clone start_time into orig_start_time
                    Schedule.this.sched.orig_start_time = Schedule.this.sched.start_time;

                    GoteFarm.goteService.saveEventSchedule(GoteFarm.sessionID, Schedule.this.sched, new AsyncCallback<Boolean>() {
                        public void onSuccess(Boolean result) {
                            errmsg.removeStyleName(errmsg.getStylePrimaryName() + "-error");
                            errmsg.setText("Schedule saved successfully.");
                        }

                        public void onFailure(Throwable caught) {
                            errmsg.addStyleName(errmsg.getStylePrimaryName() + "-error");
                            errmsg.setText(caught.getMessage());
                        }
                    });
                }
            });

            save.addStyleName(save.getStylePrimaryName() + "-bottom");
            save.addStyleName(save.getStylePrimaryName() + "-left");

            hpanel.add(save);
            hpanel.add(errmsg);

            vpanel.add(hpanel);

            initWidget(vpanel);

            setStyleName("Admin-Schedule");
        }

        public Schedule(String eventKey) {
            this(eventKey, null);
        }

        private void updateTimes() {
            Date d = new Date();

            d.setTime(sched.start_time.getTime() - display_start.getValue() * 1000);
            display_start_time.setText(time_formatter.format(d));

            d.setTime(sched.start_time.getTime() + display_end.getValue() * 1000);
            display_end_time.setText(time_formatter.format(d));

            d.setTime(sched.start_time.getTime() - signups_start.getValue() * 1000);
            signups_start_time.setText(time_formatter.format(d));

            d.setTime(sched.start_time.getTime() - signups_end.getValue() * 1000);
            signups_end_time.setText(time_formatter.format(d));
        }

        private void onClick(Object sender) {
            Widget toshow = null;
            int repeat_size = JSEventSchedule.REPEAT_NEVER;

            if      (sender == rptdaily)   { toshow = dailyrptpanel;   repeat_size = JSEventSchedule.REPEAT_DAILY; }
            else if (sender == rptweekly)  { toshow = weeklyrptpanel;  repeat_size = JSEventSchedule.REPEAT_WEEKLY; }
            else if (sender == rptmonthly) { toshow = monthlyrptpanel; repeat_size = JSEventSchedule.REPEAT_MONTHLY; }

            dailyrptpanel.setVisible(toshow == dailyrptpanel);
            weeklyrptpanel.setVisible(toshow == weeklyrptpanel);
            monthlyrptpanel.setVisible(toshow == monthlyrptpanel);

            sched.repeat_size = repeat_size;
        }

        public void onClick(ClickEvent event) {
            final Object sender = event.getSource();
            onClick(sender);
        }

        @SuppressWarnings("deprecation")
        public void onValueChange(ValueChangeEvent<Date> event) {
            final Object sender = event.getSource();
            final Date d = event.getValue();

            if (sender == db) {
                sched.start_time.setYear(d.getYear());
                sched.start_time.setMonth(d.getMonth());
                sched.start_time.setDate(d.getDate());
            }
            else if (sender == tp) {
                sched.start_time.setHours(d.getHours());
                sched.start_time.setMinutes(d.getMinutes());
                sched.start_time.setSeconds(d.getSeconds());
            }

            updateTimes();
        }
    }

    VerticalPanel vpanel = new VerticalPanel();

    public ScheduleEditor(String eventKey, List<JSEventSchedule> schedules) {
        vpanel.setWidth("100%");
        vpanel.setHeight("100%");
        vpanel.setSpacing(40);

        for (JSEventSchedule s : schedules) {
            vpanel.add(new Schedule(eventKey, s));
        }

        vpanel.add(new Schedule(eventKey));

        initWidget(vpanel);

        setStyleName("Admin-ScheduleEditor");
    }
}
