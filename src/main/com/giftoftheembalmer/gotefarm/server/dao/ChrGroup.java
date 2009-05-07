package com.giftoftheembalmer.gotefarm.server.dao;

import com.google.appengine.api.datastore.Key;

import javax.jdo.annotations.Element;
import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Order;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import java.util.ArrayList;
import java.util.List;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class ChrGroup {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;

    @Persistent
    private Account account;

    @Persistent
    private Key guildKey;

    @Persistent(mappedBy = "chrGroup")
    @Element(dependent = "true")
    @Order(extensions = @Extension(vendorName="datanucleus", key="list-ordering", value="name asc"))
    private List<Chr> characters = new ArrayList<Chr>();

    @Persistent
    private String main;

    @Persistent
    private Key mainKey;

    public ChrGroup(Account account, Key guildKey) {
        this.account = account;
        this.guildKey = guildKey;
    }

    public Account getAccount() {
        return account;
    }

    public List<Chr> getCharacters() {
        return characters;
    }

    public Key getGuildKey() {
        return guildKey;
    }

    public Key getKey() {
        return key;
    }

    public String getMain() {
        return main;
    }

    public Key getMainKey() {
        return mainKey;
    }

    public void setCharacters(List<Chr> characters) {
        this.characters = characters;
    }

    public void setMain(String main, Key mainKey) {
        this.main = main;
        this.mainKey = mainKey;
    }
}
