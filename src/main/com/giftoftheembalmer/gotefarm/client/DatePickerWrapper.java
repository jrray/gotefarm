package com.giftoftheembalmer.gotefarm.client;

import org.zenika.widget.client.datePicker.DatePicker;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SourcesChangeEvents;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import java.util.Date;
import java.util.HashSet;

public class DatePickerWrapper extends Composite
    implements ChangeListener, SourcesChangeEvents {

    private final HashSet<ChangeListener> changeListeners = new HashSet<ChangeListener>();

    class CustomDatePicker extends DatePicker {
        public CustomDatePicker(Date selectedDate) {
            super(selectedDate);
        }

        public void setSelectedDate(Date value) {
            super.setSelectedDate(value);
            notifyListeners();
        }

        public void setSelectedDateNoNotify(Date value) {
            super.setSelectedDate(value);
        }
    }

    CustomDatePicker dp;

    public DatePickerWrapper(Date selectedDate) {
        dp = new CustomDatePicker(selectedDate);
        dp.addChangeListener(this);

        initWidget(dp);
    }

    public void addChangeListener(ChangeListener listener) {
        changeListeners.add(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeListeners.remove(listener);
    }

    public void onChange(Widget sender) {
        notifyListeners();
    }

    public void setSelectedDate(Date value) {
        dp.setSelectedDateNoNotify(value);
    }

    public Date getSelectedDate() {
        return dp.getSelectedDate();
    }

    public void setYoungestDate(Date value) {
        dp.setYoungestDate(value);
    }

    private void notifyListeners() {
        for (ChangeListener cl : changeListeners) {
            cl.onChange(this);
        }
    }
}
