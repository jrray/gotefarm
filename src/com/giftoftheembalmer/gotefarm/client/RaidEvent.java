package com.giftoftheembalmer.gotefarm.client;

import java.util.Date;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class RaidEvent extends Composite {

    private Label name = new Label("Event Name");
    private Label start = new Label("Event Date");

    public RaidEvent() {
        VerticalPanel vpanel = new VerticalPanel();
        vpanel.setWidth("100%");

        // name - date
        {
            HorizontalPanel hpanel = new HorizontalPanel();
            hpanel.setSpacing(10);

            hpanel.add(name);
            hpanel.add(start);

            vpanel.add(hpanel);
        }

        // signups
        {
            Grid grid = new Grid(5, 4);
            grid.setWidth("100%");

            grid.setText(0, 1, "Tank");
            grid.setText(0, 2, "Healer");
            grid.setText(0, 3, "DPS");

            grid.setText(1, 0, "Signed Up");
            grid.setText(2, 0, "Standby");
            grid.setText(3, 0, "Possible");
            grid.setText(4, 0, "Not Coming");

            vpanel.add(grid);
        }

        initWidget(vpanel);

        setStyleName("RaidEvent");
    }

    public void setName(String name) {
        this.name.setText(name);
    }

    public void setStartDate(Date start) {
        this.start.setText(start.toString());
    }
}
