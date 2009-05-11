package com.giftoftheembalmer.gotefarm.client;

public class CharacterNotCachedException extends Exception {
    private static final long serialVersionUID = 3801684955784960322L;

    private final String character_key;

    CharacterNotCachedException(String character_key) {
        super("Character not found in cache");
        this.character_key = character_key;
    }

    public String getCharacterKey() {
        return character_key;
    }
}
