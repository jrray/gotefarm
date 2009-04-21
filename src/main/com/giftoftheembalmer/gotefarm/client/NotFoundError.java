package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.user.client.rpc.IsSerializable;

public class NotFoundError extends Exception implements IsSerializable {
    private static final long serialVersionUID = -5290390943998623984L;

    public NotFoundError(String message) {
        super(message);
    }

    public NotFoundError() {
        super();
    }
}
