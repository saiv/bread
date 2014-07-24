package it.saiv.bread.streaming;

import java.util.EventObject;
import java.nio.channels.Channel;

public class EndEvent<C extends Channel> extends EventObject {
    private static final long serialVersionUID = 2623382833469172805L;

    public EndEvent(Stream<C> stream) {
        super(stream);
    }
}
