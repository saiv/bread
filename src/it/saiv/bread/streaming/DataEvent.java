package it.saiv.bread.streaming;

import java.util.EventObject;
import java.nio.ByteBuffer;

public class DataEvent extends EventObject {
    private static final long serialVersionUID = -3437220536991642536L;

    public DataEvent(ByteBuffer data) {
        super(data);
    }
}
