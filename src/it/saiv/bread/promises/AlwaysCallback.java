package it.saiv.bread.promises;

import it.saiv.bread.promises.Promise.State;

public interface AlwaysCallback<D, R> {
    public void onAlways(final State state, final D resolved, final R rejected);
}
