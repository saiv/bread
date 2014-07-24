package it.saiv.bread.event;

import java.util.HashMap;
import java.util.EventObject;
import java.util.PriorityQueue;
import java.util.function.Consumer;

public class Emitter {
    protected HashMap<String, PriorityQueue<Consumer<EventObject>>> listeners;

    public Emitter() {
        this.listeners = new HashMap<String, PriorityQueue<Consumer<EventObject>>>();
    }

    public Emitter on(String eventName, Consumer<EventObject> listener) {
        if (!this.listeners.containsKey(eventName)) {
            this.listeners.put(eventName, new PriorityQueue<Consumer<EventObject>>());
        }

        this.listeners.get(eventName).add(listener);

        return this;
    }

    public Emitter once(String eventName, Consumer<EventObject> listener) {
        return this.on(eventName, new Consumer<EventObject>() {
            public void accept(EventObject event) {
                Emitter.this.removeListener(eventName, this);
                listener.accept(event);
            }
        });
    }

    public boolean removeListener(String eventName, Consumer<EventObject> listener) {
        if (this.listeners.containsKey(eventName)) {
            return this.listeners.get(eventName).remove(listener);
        }

        return false;
    }

    public void removeAllListeners(String eventName) {
        if (this.listeners.containsKey(eventName)) {
            this.listeners.get(eventName).clear();
        }
    }

    public void removeAllListeners() {
        this.listeners.clear();
    }

    public Emitter emit(String eventName) {
        return this.emit(eventName, new EventObject(this));
    }

    public Emitter emit(EventObject event) {
        String eventName = event.getClass().getName();

        return this.emit(eventName, event);
    }

    public Emitter emit(String eventName, EventObject event) {
        if (this.listeners.containsKey(eventName)) {
            for (Consumer<EventObject> listener : this.listeners.get(eventName)) {
                listener.accept(event);
            }
        }

        return this;
    }
}
