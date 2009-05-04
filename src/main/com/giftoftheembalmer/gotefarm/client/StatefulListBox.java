package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.ui.ListBox;

import java.util.Date;

public class StatefulListBox extends ListBox implements ChangeHandler {
    private final String name;
    // default to one year
    private Date expires = new Date(new Date().getTime() + 365L * 86400L * 1000L);

    public StatefulListBox(String name) {
        this(name, false);
    }

    public StatefulListBox(String name, boolean isMultipleSelect) {
        super(isMultipleSelect);
        this.name = "slb_" + name;
        addChangeHandler(this);
    }

    public void restoreState() {
        String value = Cookies.getCookie(name);
        if (value == null) return;
        for (int i = 0; i < getItemCount(); ++i) {
            if (getItemText(i).equals(value)) {
                setSelectedIndex(i);
                break;
            }
        }
    }

    public void onChange(ChangeEvent event) {
        int index = getSelectedIndex();
        if (index < 0) return;
        Cookies.setCookie(name, getItemText(index), expires);
    }
}
