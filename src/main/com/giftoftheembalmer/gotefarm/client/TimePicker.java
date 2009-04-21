package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;

import java.util.Date;

public class TimePicker extends Composite
    implements ChangeHandler, HasValue<Date> {

    public class TimePickerTB extends TextBox implements ClickHandler {

        private final DateTimeFormat dateFormatter;

        class PopupTime extends PopupPanel implements ChangeHandler {
            private boolean leave;
            private final TimePickerTB timePicker;
            private final ListBox lb;

            {
                this.leave = true;
                lb = new ListBox();
            }

            @SuppressWarnings("deprecation")
            public PopupTime(TimePickerTB timePicker) {
                super(true);
                this.timePicker = timePicker;
                lb.addChangeHandler(this);
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

            public void onChange(ChangeEvent event) {
                ListBox lb = (ListBox)event.getSource();
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
            addClickHandler(this);
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

        public void onClick(ClickEvent event) {
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

    public TimePicker(Date selectedDate) {
        tptb = new TimePickerTB(selectedDate);
        tptb.addChangeHandler(this);

        initWidget(tptb);
    }

    public void onChange(ChangeEvent event) {
        notifyListeners();
    }

    private void notifyListeners() {
        ValueChangeEvent.fire(this, getValue());
    }

    public Date getValue() {
        return tptb.getSelectedDate();
    }

    public void setValue(Date value) {
        setValue(value, false);
    }

    public void setValue(Date value, boolean fireEvents) {
        if (value == null) {
            throw new IllegalArgumentException("value must not be null");
        }
        if (getValue().equals(value)) {
            return;
        }
        tptb.setSelectedDate(value);
        if (fireEvents) {
            ValueChangeEvent.fire(this, value);
        }
    }

    public HandlerRegistration addValueChangeHandler(
            ValueChangeHandler<Date> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }
}
