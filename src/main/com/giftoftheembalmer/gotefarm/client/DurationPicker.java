package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

public class DurationPicker extends Composite
    implements ChangeHandler, HasValue<Integer> {

    private int duration = 0;
    private TextBox tb = new TextBox();
    private ListBox lb = new ListBox();
    private int current_unit = 0;
    private final int DAYS = 0;
    private final int HOURS = 1;
    private final int MINUTES = 2;
    private final int SECONDS = 3;

    public DurationPicker() {
        setValue(0);

        HorizontalPanel hpanel = new HorizontalPanel();
        hpanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

        tb.addChangeHandler(this);
        tb.setText("0");

        lb.addChangeHandler(this);
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
        setValue(seconds);
    }

    public void setVisibleLength(int len) {
        tb.setVisibleLength(len);
    }

    public void setValue(Integer seconds, boolean fireEvents) {
        if (seconds == null) {
            throw new IllegalArgumentException("value must not be null");
        }
        if (duration == seconds) {
            return;
        }

        duration = seconds;

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

        if (fireEvents) {
            ValueChangeEvent.fire(this, duration);
        }
    }

    public void setValue(Integer value) {
        setValue(value, false);
    }

    public void onChange(ChangeEvent event) {
        Object sender = event.getSource();
        if (sender == tb) {
            try {
                int amount = Integer.parseInt(tb.getText());
                switch (lb.getSelectedIndex()) {
                    case DAYS:
                        setValue(amount * 86400, true);
                        break;
                    case HOURS:
                        setValue(amount * 3600, true);
                        break;
                    case MINUTES:
                        setValue(amount * 60, true);
                        break;
                    case SECONDS:
                        setValue(amount, true);
                        break;
                }
            }
            catch (NumberFormatException e) {
                setValue(getValue());
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

            int unit = lb.getSelectedIndex();
            switch (unit) {
                case DAYS:
                    amount /= 86400;
                    setValue(amount * 86400, true);
                    break;
                case HOURS:
                    amount /= 3600;
                    setValue(amount * 3600, true);
                    break;
                case MINUTES:
                    amount /= 60;
                    setValue(amount * 60, true);
                    break;
                case SECONDS:
                    setValue(amount, true);
                    break;
            }

            tb.setText("" + amount);
            current_unit = unit;
        }
    }

    public Integer getValue() {
        return duration;
    }

    public HandlerRegistration addValueChangeHandler(
            ValueChangeHandler<Integer> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }
}
