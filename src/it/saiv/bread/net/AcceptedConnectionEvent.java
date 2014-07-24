package it.saiv.bread.net;

import java.util.EventObject;

public class AcceptedConnectionEvent extends EventObject {
    private static final long serialVersionUID = -8617057981831263639L;

    public AcceptedConnectionEvent(Connection connection) {
        super(connection);
    }

    public Connection getConnection() {
        return (Connection) this.getSource();
    }
}
