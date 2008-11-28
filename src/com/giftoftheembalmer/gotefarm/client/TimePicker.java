package com.giftoftheembalmer.gotefarm.client;

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

public class TimePicker extends Composite
    implements ChangeListener, SourcesChangeEvents {

    public class TimePickerTB extends TextBox implements ClickListener {

        private final DateTimeFormat dateFormatter;

        class PopupTime extends PopupPanel implements ChangeListener {
            private boolean leave;
            private final TimePickerTB timePicker;
            private final ListBox lb;

            {
                this.leave = true;
                lb = new ListBox();
            }

            public PopupTime(TimePickerTB timePicker) {
                super(true);
                this.timePicker = timePicker;
                lb.addChangeListener(this);
                lb.setVisibleItemCount(10);
                Date d = new Date();
                for (int hour = 0; hour < 24; ++hour) {
                    d.setHours(hour);

                    d.setMinutes(0);
                    lb.addItem(dateFormatter.format(d));

                    d.setMinutes(30);
                    lb.addItem(dateFormatter.format(d));
                }
                this.add(lb);

                sinkEvents(Event.ONBLUR);
            }

            public void hidePopup() {
                DeferredCommand.addCommand(new Command() {
                    public void execute() {
                        Timer t = new Timer() {
                            public void run() {
                                if (leave) {
                                    hide();
                                }
                                else {
                                    leave = true;
                                }
                            }
                        };
                        t.schedule(300);
                    }
                });
            }

            public void displayTime() {
                show();
            }

            public void setDisplayedTime(Date d) {
                String t = dateFormatter.format(d);
                for (int i = 0; i < lb.getItemCount(); ++i) {
                    if (lb.getItemText(i).equals(t)) {
                        lb.setSelectedIndex(i);
                        break;
                    }
                }
            }

            public void onChange(Widget sender) {
                ListBox lb = (ListBox)sender;
                Date sel = dateFormatter.parse(lb.getItemText(lb.getSelectedIndex()));
                timePicker.setSelectedDate(sel);
                timePicker.synchronizeFromDate();
                hide();
                leave = true;
                notifyListeners();
            }
        }

        private PopupTime popup;
        private Date selectedDate;

        {
            dateFormatter = DateTimeFormat.getFormat("HH:mm");
            popup = new PopupTime(this);
        }

        public TimePickerTB() {
            super();
            setText("");
            sinkEvents(Event.ONCHANGE | Event.ONKEYPRESS);
            addClickListener(this);
        }

        public TimePickerTB(Date selectedDate) {
            this();
            this.selectedDate = selectedDate;
            synchronizeFromDate();
        }

        public void onBrowserEvent(Event event) {
            switch (DOM.eventGetType(event)) {
                case Event.ONBLUR:
                    popup.hidePopup();
                    break;
                case Event.ONCHANGE:
                    parseDate();
                    break;
                case Event.ONKEYPRESS:
                    if (DOM.eventGetKeyCode(event) == 13) {
                        parseDate();
                        showPopup();
                    }
                    break;
            }
            super.onBrowserEvent(event);
        }

        public void onClick(Widget sender) {
            showPopup();
        }

        public void synchronizeFromDate() {
            if (this.selectedDate != null) {
                this.setText(dateFormatter.format(this.selectedDate));
            }
            else {
                this.setText("");
            }
        }

        public void showPopup() {
            if (this.selectedDate != null) {
                popup.setDisplayedTime(this.selectedDate);
            }
            popup.setPopupPosition(this.getAbsoluteLeft()+150, this.getAbsoluteTop());
            popup.displayTime();
        }

        public void parseDate() {
            try {
                this.selectedDate = dateFormatter.parse(getText());
            }
            catch (IllegalArgumentException e) {
            }
            synchronizeFromDate();
        }

        public void setSelectedDate(Date value) {
            this.selectedDate = value;
            synchronizeFromDate();
        }

        public Date getSelectedDate() {
            return (Date)this.selectedDate.clone();
        }
    }

    TimePickerTB tptb;

    private final HashSet<ChangeListener> changeListeners = new HashSet<ChangeListener>();

    public TimePicker(Date selectedDate) {
        tptb = new TimePickerTB(selectedDate);
        tptb.addChangeListener(this);

        initWidget(tptb);
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
        tptb.setSelectedDate(value);
    }

    public Date getSelectedDate() {
        return tptb.getSelectedDate();
    }

    private void notifyListeners() {
        for (ChangeListener cl : changeListeners) {
            cl.onChange(this);
        }
    }
}
