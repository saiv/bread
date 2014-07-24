package it.saiv.bread.event.loop;

import it.saiv.bread.event.Loop;

import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.nio.channels.SelectableChannel;
import java.nio.channels.ClosedChannelException;
import java.io.IOException;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.Iterator;

public class SelectorLoop implements Loop<SelectableChannel> {
    private boolean running;
    private Selector acceptSelector;
    private Selector readSelector;
    private Selector writeSelector;
    private HashMap<SelectionKey, Consumer<SelectableChannel>> consumers;

    /*
     * private NextTickQueue nextTickQueue; private FutureTickQueue
     * futureTickQueue; private TimersScheduler timersScheduler;
     */

    public SelectorLoop() throws IOException {
        this.running = false;
        this.acceptSelector = Selector.open();
        this.readSelector = Selector.open();
        this.writeSelector = Selector.open();
        this.consumers = new HashMap<SelectionKey, Consumer<SelectableChannel>>();
        /*
         * this.nextTickQueue = new NextTickQueue(this); this.futureTickQueue =
         * new FutureTickQueue(this); this.timersScheduler = new
         * TimersScheduler();
         */
    }

    public void addAcceptChannel(SelectableChannel channel, Consumer<SelectableChannel> consumer) throws ClosedChannelException {
        if (null == channel.keyFor(this.acceptSelector)) {
            SelectionKey key = channel.register(this.acceptSelector, SelectionKey.OP_ACCEPT);
            this.consumers.put(key, consumer);
        }
    }

    public void addReadChannel(SelectableChannel channel, Consumer<SelectableChannel> consumer) throws ClosedChannelException {
        if (null == channel.keyFor(this.readSelector)) {
            SelectionKey key = channel.register(this.readSelector, SelectionKey.OP_READ);
            this.consumers.put(key, consumer);
        }
    }

    public void addWriteChannel(SelectableChannel channel, Consumer<SelectableChannel> consumer) throws ClosedChannelException {
        if (null == channel.keyFor(this.writeSelector)) {
            SelectionKey key = channel.register(this.writeSelector, SelectionKey.OP_WRITE);
            this.consumers.put(key, consumer);
        }
    }

    public void removeAcceptChannel(SelectableChannel channel) {
        SelectionKey key = channel.keyFor(this.acceptSelector);
        if (null != key) {
            key.cancel();
        }
    }

    public void removeReadChannel(SelectableChannel channel) {
        SelectionKey key = channel.keyFor(this.readSelector);
        if (null != key) {
            key.cancel();
        }
    }

    public void removeWriteChannel(SelectableChannel channel) {
        SelectionKey key = channel.keyFor(this.writeSelector);
        if (null != key) {
            key.cancel();
        }
    }

    public void removeChannel(SelectableChannel channel) {
        this.removeAcceptChannel(channel);
        this.removeReadChannel(channel);
        this.removeWriteChannel(channel);
    }

    /*
     * public void nextTick(Consumer<SelectableChannel> listener) {
     * this.nextTickQueue.add(listener); }
     * 
     * public void futureTick(Consumer<SelectableChannel> listener) {
     * this.futureTickQueue.add(listener); }
     * 
     * public void tick() { this.nextTickQueue.tick();
     * this.futureTickQueue.tick(); this.timerScheduler.tick();
     * this.waitForChannelActivity(0); }
     */
    private void waitForStreamActivity(int timeout) throws IOException {
        this.acceptSelector.selectNow();
        this.readSelector.selectNow();
        this.writeSelector.selectNow();

        Iterator<SelectionKey> acceptSelectedKeys = this.acceptSelector.selectedKeys().iterator();
        Iterator<SelectionKey> readSelectedKeys = this.readSelector.selectedKeys().iterator();
        Iterator<SelectionKey> writeSelectedKeys = this.writeSelector.selectedKeys().iterator();

        this.consumeSelectedChannels(acceptSelectedKeys);
        this.consumeSelectedChannels(readSelectedKeys);
        this.consumeSelectedChannels(writeSelectedKeys);
    }

    private void consumeSelectedChannels(Iterator<SelectionKey> selectedKeys) {
        while (selectedKeys.hasNext()) {
            SelectionKey key = (SelectionKey) selectedKeys.next();
            selectedKeys.remove();

            if (!key.isValid()) {
                continue;
            } else if (key.isAcceptable() || key.isReadable() || key.isWritable()) {
                Consumer<SelectableChannel> consumer = this.consumers.get(key);
                consumer.accept(key.channel());
            }
        }
    }

    public void start() {
        this.running = true;
    }

    public void stop() {
        this.running = false;
    }

    public void run() {
        this.start();

        int timeout = 0;

        while (this.running) {
            /*
             * this.nextTickQueue.tick(); this.futureTickQueue.tick();
             * this.timers.tick();
             * 
             * if (!this.running || this.nextTickQueue.isEmpty() ||
             * this.futureTickQueue.isEmpty()) { timeout = 0; } else if (int
             * scheduledAt = this.timers.getFirst()) { if (0 > timeout =
             * scheduledAt - this.timers.getTime()) { timeout = 0; } } else if
             * (!this.readStreams.isEmpty() || !this.writeStreams.isEmpty() {
             * timeout = null; } else { break; }
             */

            try {
                this.waitForStreamActivity(timeout * 1000);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
}
