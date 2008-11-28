package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.user.client.rpc.IsSerializable;

public class AlreadyExistsError extends Exception implements IsSerializable {
    public AlreadyExistsError(String message) {
        super(message);
    }

    public AlreadyExistsError() {
        super();
    }
}
