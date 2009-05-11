package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CharacterCache extends HashMap<String, JSCharacter> {
    private static final long serialVersionUID = -1388242871532769630L;

    private
    final Map<String, List<AsyncCallback<JSCharacter>>> pending_requests
                    = new HashMap<String, List<AsyncCallback<JSCharacter>>>();

    JSCharacter get(String key) throws CharacterNotCachedException {
        JSCharacter chr = super.get(key);
        if (chr == null) {
            throw new CharacterNotCachedException(key);
        }
        return chr;
    }

    public void getCharacter(final String key,
                             AsyncCallback<JSCharacter> asyncCallback) {
        // if a request for this character has gone out, multiplex the
        // response to this new asyncCallback.
        List<AsyncCallback<JSCharacter>> callbacks = pending_requests.get(key);
        if (callbacks == null) {
            // create a new list
            callbacks = new ArrayList<AsyncCallback<JSCharacter>>();
            pending_requests.put(key, callbacks);

            // add the caller's callback
            callbacks.add(asyncCallback);

            // no pending request, start one
            GWT.log("Requesting missing cached character: " + key, null);
            GoteFarm.goteService.getCharacter(key,
                                              new AsyncCallback<JSCharacter>() {
                public void onSuccess(JSCharacter chr) {
                    // cache the result!
                    put(chr.key, chr);

                    // fire off all the callbacks
                    List<AsyncCallback<JSCharacter>> callbacks
                        = pending_requests.remove(key);
                    if (callbacks == null) {
                        GWT.log("Unexpected lack of callbacks.", null);
                        return;
                    }

                    for (AsyncCallback<JSCharacter> cb : callbacks) {
                        try {
                            cb.onSuccess(chr);
                        }
                        catch (Throwable e) {
                            GWT.log(  "Consuming exception after getting a"
                                    + " cached character.", e);
                        }
                    }
                }

                public void onFailure(Throwable caught) {
                    // fire off all the callbacks
                    List<AsyncCallback<JSCharacter>> callbacks
                        = pending_requests.remove(key);
                    if (callbacks == null) {
                        GWT.log("Unexpected lack of callbacks.", null);
                        return;
                    }

                    for (AsyncCallback<JSCharacter> cb : callbacks) {
                        try {
                            cb.onFailure(caught);
                        }
                        catch (Throwable e) {
                            GWT.log(  "Consuming exception after getting a"
                                    + " cached character.", e);
                        }
                    }
                }
            });
        }
        else {
            GWT.log("Existing request for missing cached character: " + key,
                    null);
            // add the caller's callback
            callbacks.add(asyncCallback);
        }
    }
}
