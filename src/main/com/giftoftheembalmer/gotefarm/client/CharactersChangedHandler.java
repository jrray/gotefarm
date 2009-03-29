package com.giftoftheembalmer.gotefarm.client;

import java.util.EventListener;

public interface CharactersChangedHandler
    extends EventListener {
    void onCharactersChanged(CharactersChangedEvent event);
}
