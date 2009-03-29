package com.giftoftheembalmer.gotefarm.client;

public interface FiresCharactersChangedEvents {
    void addEventHandler(CharactersChangedHandler handler);
    void removeEventHandler(CharactersChangedHandler handler);
}
