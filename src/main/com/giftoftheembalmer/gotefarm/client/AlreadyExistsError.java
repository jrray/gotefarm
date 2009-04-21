package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.user.client.rpc.IsSerializable;

public class AlreadyExistsError extends Exception implements IsSerializable {
    private static final long serialVersionUID = -260706808729936840L;

    public AlreadyExistsError(String message) {
        super(message);
    }

    public AlreadyExistsError() {
        super();
    }
}
