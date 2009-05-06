package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.user.client.rpc.IsSerializable;

public class NotAuthorizedException extends Exception
    implements IsSerializable {
    private static final long serialVersionUID = -5855774815748974108L;

    public NotAuthorizedException(String message) {
        super(message);
    }

    public NotAuthorizedException() {
        super();
    }
}
