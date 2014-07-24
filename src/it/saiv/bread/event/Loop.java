package it.saiv.bread.event;

import java.nio.channels.Channel;
import java.nio.channels.ClosedChannelException;
import java.util.function.Consumer;

public interface Loop<C extends Channel> extends Runnable {
    public void addAcceptChannel(C channel, Consumer<C> consumer) throws ClosedChannelException;

    public void addReadChannel(C channel, Consumer<C> consumer) throws ClosedChannelException;

    public void addWriteChannel(C channel, Consumer<C> consumer) throws ClosedChannelException;

    public void removeAcceptChannel(C channel);

    public void removeReadChannel(C channel);

    public void removeWriteChannel(C channel);

    public void removeChannel(C channel);

    public void start();

    public void stop();
}
