package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.Date;
import java.util.List;

public class ScheduleEditor extends Composite {
    class Schedule extends Composite implements ChangeListener, ClickListener {
        JSEventSchedule sched = null;
        VerticalPanel vpanel = new VerticalPanel();
        final DatePickerWrapper dp;
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
        final RadioButton rptnever = new RadioButton("repeatGroup", "Never");
        final RadioButton rptdaily = new RadioButton("repeatGroup", "Daily");
        final RadioButton rptweekly = new RadioButton("repeatGroup", "Weekly");
        final RadioButton rptmonthly = new RadioButton("repeatGroup", "Monthly");
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
        final RadioButton rptdayofmonth = new RadioButton("dayofGroup", "day of the month");
        final RadioButton rptdayofweek = new RadioButton("dayofGroup", "day of the week");

        public Schedule(JSEventSchedule sched) {
            this.sched = sched;
            if (this.sched == null) {
                this.sched = sched = new JSEventSchedule();

                sched.esid = -1;
                sched.eid = -1;

                sched.start_time = new Date();
                sched.start_time.setTime(
                    sched.start_time.getTime() -
                    (sched.start_time.getTime() % 3600000)
                );

                sched.duration = 120 * 60;

                sched.display_start = 7 * 86400;
                sched.display_end = 3600;

                sched.signups_start = 2 * 86400;
                sched.signups_end = 1800;

                sched.repeat_size = 0;
                sched.repeat_freq = 1;
                sched.day_mask = 1 << sched.start_time.getDay();
                sched.repeat_by = 0;
            }

            vpanel.setWidth("100%");

            dp = new DatePickerWrapper(sched.start_time);
            dp.setYoungestDate(new Date());
            dp.addChangeListener(this);
            tp = new TimePicker(sched.start_time);
            tp.addChangeListener(this);

            {
                HorizontalPanel hpanel = new HorizontalPanel();
                hpanel.setVerticalAlignment(hpanel.ALIGN_MIDDLE);
                hpanel.add(new Label("Start Date:"));
                hpanel.add(dp);
                hpanel.add(new HTML("&nbsp;&nbsp;"));
                hpanel.add(new Label("Start Time:"));
                hpanel.add(tp);

                vpanel.add(hpanel);
            }

            display_start.addChangeListener(this);
            display_start.setVisibleLength(6);
            display_start.setDuration(sched.display_start);

            {
                HorizontalPanel hpanel = new HorizontalPanel();
                hpanel.setVerticalAlignment(hpanel.ALIGN_MIDDLE);
                hpanel.setSpacing(5);
                hpanel.add(new Label("Display event "));
                hpanel.add(display_start);
                hpanel.add(new Label(" before start time: "));
                hpanel.add(display_start_time);

                vpanel.add(hpanel);
            }

            display_end.addChangeListener(this);
            display_end.setVisibleLength(6);
            display_end.setDuration(sched.display_end);

            {
                HorizontalPanel hpanel = new HorizontalPanel();
                hpanel.setVerticalAlignment(hpanel.ALIGN_MIDDLE);
                hpanel.setSpacing(5);
                hpanel.add(new Label("Remove event "));
                hpanel.add(display_end);
                hpanel.add(new Label(" after start time: "));
                hpanel.add(display_end_time);

                vpanel.add(hpanel);
            }

            signups_start.addChangeListener(this);
            signups_start.setVisibleLength(6);
            signups_start.setDuration(sched.signups_start);

            {
                HorizontalPanel hpanel = new HorizontalPanel();
                hpanel.setVerticalAlignment(hpanel.ALIGN_MIDDLE);
                hpanel.setSpacing(5);
                hpanel.add(new Label("Signups open "));
                hpanel.add(signups_start);
                hpanel.add(new Label(" before start time: "));
                hpanel.add(signups_start_time);

                vpanel.add(hpanel);
            }

            signups_end.addChangeListener(this);
            signups_end.setVisibleLength(6);
            signups_end.setDuration(sched.signups_end);

            {
                HorizontalPanel hpanel = new HorizontalPanel();
                hpanel.setVerticalAlignment(hpanel.ALIGN_MIDDLE);
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

                rptnever.addClickListener(this);
                rptdaily.addClickListener(this);
                rptweekly.addClickListener(this);
                rptmonthly.addClickListener(this);

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

                dailyrptdays.addChangeListener(new ChangeListener() {
                    public void onChange(Widget sender) {
                        if (dailyrptdays.getSelectedIndex() > 0) {
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

                weeklyrptweeks.addChangeListener(new ChangeListener() {
                    public void onChange(Widget sender) {
                        if (weeklyrptweeks.getSelectedIndex() > 0) {
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

                for (int i = 0; i < 7; ++i ) {
                    weeklydays[i].addStyleName("padleft");
                    if ((sched.day_mask & (1 << i)) > 0) {
                        weeklydays[i].setChecked(true);
                    }
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

                monthlyrptmonth.addChangeListener(new ChangeListener() {
                    public void onChange(Widget sender) {
                        if (monthlyrptmonth.getSelectedIndex() > 0) {
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

                if (sched.repeat_by == 0) {
                    rptdayofmonth.setChecked(true);
                }
                else {
                    rptdayofweek.setChecked(true);
                }

                hpanel.add(rptdayofmonth);
                hpanel.add(rptdayofweek);

                monthlyrptpanel.add(hpanel);
                vpanel.add(monthlyrptpanel);
            }

            updateTimes();

            switch (sched.repeat_size) {
                case 0:
                    rptnever.setChecked(true);
                    break;
                case 1:
                    rptdaily.setChecked(true);
                    onClick(rptdaily);
                    break;
                case 2:
                    rptweekly.setChecked(true);
                    onClick(rptweekly);
                    break;
                case 3:
                    rptmonthly.setChecked(true);
                    onClick(rptmonthly);
                    break;
            }

            initWidget(vpanel);

            setStyleName("Admin-Schedule");
        }

        public Schedule() {
            this(null);
        }

        public void onChange(Widget sender) {
            if (sched == null) return;

            if (sender == dp) {
                Date d = dp.getSelectedDate();
                sched.start_time.setYear(d.getYear());
                sched.start_time.setMonth(d.getMonth());
                sched.start_time.setDate(d.getDate());
            }
            else if (sender == tp) {
                Date d = tp.getSelectedDate();
                sched.start_time.setHours(d.getHours());
                sched.start_time.setMinutes(d.getMinutes());
                sched.start_time.setSeconds(d.getSeconds());
            }
            else if (sender == display_start) {
                sched.display_start = display_start.getDuration();
            }
            else if (sender == display_end) {
                sched.display_end = display_end.getDuration();
            }
            else if (sender == signups_start) {
                sched.signups_start = signups_start.getDuration();
            }
            else if (sender == signups_end) {
                sched.signups_end = signups_end.getDuration();
            }

            updateTimes();
        }

        private void updateTimes() {
            Date d = new Date();

            d.setTime(sched.start_time.getTime() - display_start.getDuration() * 1000);
            display_start_time.setText(time_formatter.format(d));

            d.setTime(sched.start_time.getTime() + display_end.getDuration() * 1000);
            display_end_time.setText(time_formatter.format(d));

            d.setTime(sched.start_time.getTime() - signups_start.getDuration() * 1000);
            signups_start_time.setText(time_formatter.format(d));

            d.setTime(sched.start_time.getTime() - signups_end.getDuration() * 1000);
            signups_end_time.setText(time_formatter.format(d));
        }

        public void onClick(Widget sender) {
            Widget toshow = null;

            if (sender == rptdaily)        { toshow = dailyrptpanel; }
            else if (sender == rptweekly)  { toshow = weeklyrptpanel; }
            else if (sender == rptmonthly) { toshow = monthlyrptpanel; }

            dailyrptpanel.setVisible(toshow == dailyrptpanel);
            weeklyrptpanel.setVisible(toshow == weeklyrptpanel);
            monthlyrptpanel.setVisible(toshow == monthlyrptpanel);
        }
    }

    VerticalPanel vpanel = new VerticalPanel();

    public ScheduleEditor(List<JSEventSchedule> schedules) {
        vpanel.setWidth("100%");
        vpanel.setHeight("100%");

        for (JSEventSchedule s : schedules) {
            vpanel.add(new Schedule(s));
        }

        vpanel.add(new Schedule());

        initWidget(vpanel);

        setStyleName("Admin-ScheduleEditor");
    }
}
