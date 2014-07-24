package it.saiv.bread.promises;

public interface Promise<D, F, P> {
    public enum State {
        PENDING, REJECTED, RESOLVED
    }

    public State state();

    public boolean isPending();

    public boolean isResolved();

    public boolean isRejected();

    public Promise<D, F, P> then(DoneCallback<D> doneCallback);

    public Promise<D, F, P> then(DoneCallback<D> doneCallback, FailCallback<F> failCallback);

    public Promise<D, F, P> then(DoneCallback<D> doneCallback, FailCallback<F> failCallback, ProgressCallback<P> progressCallback);

    public Promise<D, F, P> done(DoneCallback<D> callback);

    public Promise<D, F, P> fail(FailCallback<F> callback);

    public Promise<D, F, P> always(AlwaysCallback<D, F> callback);

    public Promise<D, F, P> progress(ProgressCallback<P> callback);
}
