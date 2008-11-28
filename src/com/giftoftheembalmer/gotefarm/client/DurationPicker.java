package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SourcesChangeEvents;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import java.util.HashSet;

public class DurationPicker extends Composite
    implements ChangeListener, SourcesChangeEvents {

    private int duration = 0;
    private TextBox tb = new TextBox();
    private ListBox lb = new ListBox();
    private int current_unit = 0;
    private final int DAYS = 0;
    private final int HOURS = 1;
    private final int MINUTES = 2;
    private final int SECONDS = 3;

    private final HashSet<ChangeListener> changeListeners = new HashSet<ChangeListener>();

    public DurationPicker() {
        HorizontalPanel hpanel = new HorizontalPanel();
        hpanel.setVerticalAlignment(hpanel.ALIGN_MIDDLE);

        tb.addChangeListener(this);
        tb.setText("0");

        lb.addChangeListener(this);
        lb.addItem("Days");
        lb.addItem("Hours");
        lb.addItem("Minutes");
        lb.addItem("Seconds");
        lb.setSelectedIndex(SECONDS);

        hpanel.add(tb);
        hpanel.add(lb);

        initWidget(hpanel);

        setStyleName("DurationPicker");
    }

    public DurationPicker(int seconds) {
        this();
        setDuration(seconds);
    }

    public void setVisibleLength(int len) {
        tb.setVisibleLength(len);
    }

    public void setDuration(int seconds) {
        this.duration = seconds;

        if ((seconds % 86400) == 0) {
            int days = seconds / 86400;
            tb.setText("" + days);
            lb.setSelectedIndex(DAYS);
            current_unit = DAYS;
        }
        else if ((seconds % 3600) == 0) {
            int hours = seconds / 3600;
            tb.setText("" + hours);
            lb.setSelectedIndex(HOURS);
            current_unit = HOURS;
        }
        else if ((seconds % 60) == 0) {
            int minutes = seconds / 60;
            tb.setText("" + minutes);
            lb.setSelectedIndex(MINUTES);
            current_unit = MINUTES;
        }
        else {
            tb.setText("" + seconds);
            lb.setSelectedIndex(SECONDS);
            current_unit = SECONDS;
        }
    }

    public int getDuration() {
        return duration;
    }

    public void onChange(Widget sender) {
        if (sender == tb) {
            try {
                int old_duration = duration;

                int amount = Integer.parseInt(tb.getText());
                switch (lb.getSelectedIndex()) {
                    case DAYS:
                        duration = amount * 86400;
                        break;
                    case HOURS:
                        duration = amount * 3600;
                        break;
                    case MINUTES:
                        duration = amount * 60;
                        break;
                    case SECONDS:
                        duration = amount;
                        break;
                }

                if (duration != old_duration) {
                    notifyListeners();
                }
            }
            catch (NumberFormatException e) {
                setDuration(duration);
            }
        }
        else if (sender == lb) {
            int amount = Integer.parseInt(tb.getText());
            switch (current_unit) {
                case DAYS:
                    amount *= 86400;
                    break;
                case HOURS:
                    amount *= 3600;
                    break;
                case MINUTES:
                    amount *= 60;
                    break;
                case SECONDS:
                    break;
            }

            int old_duration = duration;

            int unit = lb.getSelectedIndex();
            switch (unit) {
                case DAYS:
                    amount /= 86400;
                    duration = amount * 86400;
                    break;
                case HOURS:
                    amount /= 3600;
                    duration = amount * 3600;
                    break;
                case MINUTES:
                    amount /= 60;
                    duration = amount * 60;
                    break;
                case SECONDS:
                    duration = amount;
                    break;
            }

            tb.setText("" + amount);
            current_unit = unit;

            if (duration != old_duration) {
                notifyListeners();
            }
        }
    }

    public void addChangeListener(ChangeListener listener) {
        changeListeners.add(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeListeners.remove(listener);
    }

    private void notifyListeners() {
        for (ChangeListener cl : changeListeners) {
            cl.onChange(this);
        }
    }
}
