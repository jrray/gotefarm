package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.user.client.rpc.IsSerializable;

public class NotFoundError extends Exception implements IsSerializable {
    public NotFoundError(String message) {
        super(message);
    }

    public NotFoundError() {
        super();
    }
}
