package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.user.client.ui.Widget;

import java.util.EventObject;
import java.util.List;

public class CharactersChangedEvent
    extends EventObject {

    private List<JSCharacter> characters;

    CharactersChangedEvent(Widget sender, List<JSCharacter> characters) {
        super(sender);
        this.characters = characters;
    }

    List<JSCharacter> getCharacters() {
        return characters;
    }
}
