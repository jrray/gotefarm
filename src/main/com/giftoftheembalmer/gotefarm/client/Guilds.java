package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class Guilds extends Composite implements HasValue<JSGuild> {

    private JSAccount account;
    private JSGuild active_guild;
    VerticalPanel vpanel = new VerticalPanel();

    public Guilds() {
        vpanel.setWidth("100%");

        vpanel.add(new Label("Loading ..."));

        initWidget(vpanel);

        setStyleName("Guilds");

        GoteFarm.goteService.getAccount(new AsyncCallback<JSAccount>() {
            public void onSuccess(JSAccount result) {
                account = result;

                vpanel.clear();

                if (result.guilds.isEmpty()) {
                    enroll();
                    return;
                }

                showGuilds(result);
            }

            public void onFailure(Throwable caught) {
                vpanel.clear();
                Label errmsg = new Label(caught.getMessage());
                errmsg.addStyleName("error");
                vpanel.add(errmsg);
            }
        });
    }

    void showGuilds(JSAccount account) {
        vpanel.clear();
        ListBox lb = new ListBox();
        // TODO: Watch for selection changes and update the user's
        // current_guild
        lb.setVisibleItemCount(5);
        for (JSGuild g : account.guilds) {
            lb.addItem(g.name);
            if (   account.active_guild != null
                && g.key.equals(account.active_guild.key)) {
                lb.setSelectedIndex(lb.getItemCount()-1);
            }
        }
        vpanel.add(lb);
        // TODO: Add a way for a user to add additional guilds

        setValue(account.active_guild, true);
    }

    void enroll() {
        vpanel.clear();

        vpanel.add(new Label("You need to create or join a guild."));
        vpanel.add(new HTML("&nbsp;"));
        vpanel.add(new Label(  "To get started, paste the armory URL of your "
                             + "main character who is a member of the guild "
                             + "you would like to register here."));
        HorizontalPanel hpanel = new HorizontalPanel();
        hpanel.setSpacing(10);
        hpanel.add(new Label("Armory URL:"));
        final TextBox tb = new TextBox();
        tb.setMaxLength(100);
        tb.setVisibleLength(75);
        hpanel.add(tb);
        vpanel.add(hpanel);
        final Label errmsg = new Label();
        errmsg.addStyleName("error");
        vpanel.add(errmsg);

        tb.addValueChangeHandler(new ValueChangeHandler<String>() {
            public void onValueChange(ValueChangeEvent<String> event) {
                final String url = event.getValue();
                if (!url.contains("wowarmory.com/")) {
                    errmsg.setText("URL does not appear to be an armory URL");
                    return;
                }

                tb.setEnabled(false);
                errmsg.setText("");

                GoteFarm.goteService.getGuildFromArmoryURL(tb.getText(), new AsyncCallback<JSGuild>() {
                    public void onSuccess(JSGuild result) {
                        account.guilds.add(result);
                        account.active_guild = result;
                        showGuilds(account);
                    }

                    public void onFailure(Throwable caught) {
                        tb.setEnabled(true);
                        errmsg.setText(caught.getMessage());
                    }
                });
            }
        });
    }

    public JSGuild getValue() {
        return active_guild;
    }

    public void setValue(JSGuild value) {
        setValue(value, false);
    }

    public void setValue(JSGuild value, boolean fireEvents) {
        if (value == null) {
            if (active_guild == null) {
                return;
            }
        }
        else if (value.equals(active_guild)) {
            return;
        }

        active_guild = value;

        if (fireEvents) {
            ValueChangeEvent.fire(this, value);
        }
    }

    public HandlerRegistration addValueChangeHandler(
            ValueChangeHandler<JSGuild> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }
}
