package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.List;

public class JSAccount implements IsSerializable {

    public String key;
    public String email;
    public List<JSGuild> guilds;
    public JSGuild active_guild;

    public JSAccount() {
    }
}
