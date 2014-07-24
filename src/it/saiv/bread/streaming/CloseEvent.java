package it.saiv.bread.streaming;

import java.util.EventObject;
import java.nio.channels.Channel;

public class CloseEvent<C extends Channel> extends EventObject {
    private static final long serialVersionUID = -3605716012598210586L;

    public CloseEvent(Stream<C> stream) {
        super(stream);
    }
}
