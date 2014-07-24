package it.saiv.bread.promises;

public interface Deferred<D, F, P> extends Promise<D, F, P> {
    Deferred<D, F, P> resolve(final D resolve);

    Deferred<D, F, P> reject(final F reject);

    Deferred<D, F, P> update(final P update);

    Promise<D, F, P> promise();
}
