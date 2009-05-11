package com.giftoftheembalmer.gotefarm.client;

import java.util.HashMap;

public final class CharacterCache extends HashMap<String, JSCharacter> {
    private static final long serialVersionUID = -1388242871532769630L;

    JSCharacter get(String key) throws CharacterNotCachedException {
        JSCharacter chr = super.get(key);
        if (chr == null) {
            throw new CharacterNotCachedException(key);
        }
        return chr;
    }
}
