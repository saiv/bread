package it.saiv.bread.promises;

public class DeferredPromise<D, F, P> implements Promise<D, F, P> {
    private final Promise<D, F, P> promise;
    protected final Deferred<D, F, P> deferred;

    public DeferredPromise(Deferred<D, F, P> deferred) {
        this.deferred = deferred;
        this.promise = deferred.promise();
    }

    public State state() {
        return this.promise.state();
    }

    public boolean isPending() {
        return this.promise.isPending();
    }

    public boolean isResolved() {
        return this.promise.isResolved();
    }

    public boolean isRejected() {
        return this.promise.isRejected();
    }

    public Promise<D, F, P> then(DoneCallback<D> doneCallback) {
        return this.promise.then(doneCallback);
    }

    public Promise<D, F, P> then(DoneCallback<D> doneCallback, FailCallback<F> failCallback) {
        return this.promise.then(doneCallback, failCallback);
    }

    public Promise<D, F, P> then(DoneCallback<D> doneCallback, FailCallback<F> failCallback, ProgressCallback<P> progressCallback) {
        return this.promise.then(doneCallback, failCallback, progressCallback);
    }

    public Promise<D, F, P> done(DoneCallback<D> callback) {
        return this.promise.done(callback);
    }

    public Promise<D, F, P> fail(FailCallback<F> callback) {
        return this.promise.fail(callback);
    }

    public Promise<D, F, P> always(AlwaysCallback<D, F> callback) {
        return this.promise.always(callback);
    }

    public Promise<D, F, P> progress(ProgressCallback<P> callback) {
        return this.promise.progress(callback);
    }
}
