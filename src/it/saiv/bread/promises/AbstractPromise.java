package it.saiv.bread.promises;

import java.util.List;
import java.util.ArrayList;

public abstract class AbstractPromise<D, F, P> implements Promise<D, F, P> {
    protected volatile State state = State.PENDING;

    protected final List<DoneCallback<D>> doneCallbacks = new ArrayList<DoneCallback<D>>();
    protected final List<FailCallback<F>> failCallbacks = new ArrayList<FailCallback<F>>();
    protected final List<ProgressCallback<P>> progressCallbacks = new ArrayList<ProgressCallback<P>>();
    protected final List<AlwaysCallback<D, F>> alwaysCallbacks = new ArrayList<AlwaysCallback<D, F>>();

    protected D resolveResult;
    protected F rejectResult;

    public State state() {
        return this.state;
    }

    public Promise<D, F, P> done(DoneCallback<D> callback) {
        this.doneCallbacks.add(callback);
        if (this.isResolved()) {
            this.triggerDone(callback, this.resolveResult);
        }
        return this;
    }

    public Promise<D, F, P> fail(FailCallback<F> callback) {
        this.failCallbacks.add(callback);
        if (this.isRejected()) {
            this.triggerFail(callback, this.rejectResult);
        }
        return this;
    }

    public Promise<D, F, P> always(AlwaysCallback<D, F> callback) {
        this.alwaysCallbacks.add(callback);
        if (!this.isPending()) {
            this.triggerAlways(callback, this.state, this.resolveResult, this.rejectResult);
        }
        return this;
    }

    protected void triggerDone(D resolved) {
        for (DoneCallback<D> callback : this.doneCallbacks) {
            try {
                this.triggerDone(callback, resolved);
            } catch (Exception e) {
                System.out.println("an uncaught exception occured in a DoneCallback");
            }
        }
    }

    protected void triggerDone(DoneCallback<D> callback, D resolved) {
        callback.onDone(resolved);
    }

    protected void triggerFail(F rejected) {
        for (FailCallback<F> callback : this.failCallbacks) {
            try {
                this.triggerFail(callback, rejected);
            } catch (Exception e) {
                System.out.println("an uncaught exception occured in a FailCallback");
            }
        }
    }

    protected void triggerFail(FailCallback<F> callback, F rejected) {
        callback.onFail(rejected);
    }

    protected void triggerProgress(P progress) {
        for (ProgressCallback<P> callback : this.progressCallbacks) {
            try {
                this.triggerProgress(callback, progress);
            } catch (Exception e) {
                System.out.println("an uncaught exception occured in a ProgressCallback");
            }
        }
    }

    protected void triggerProgress(ProgressCallback<P> callback, P progress) {
        callback.onProgress(progress);
    }

    protected void triggerAlways(State state, D resolve, F reject) {
        for (AlwaysCallback<D, F> callback : this.alwaysCallbacks) {
            try {
                triggerAlways(callback, state, resolve, reject);
            } catch (Exception e) {
                System.out.println("an uncaught exception occured in a AlwaysCallback");
            }
        }
    }

    protected void triggerAlways(AlwaysCallback<D, F> callback, State state, D resolve, F reject) {
        callback.onAlways(state, resolve, reject);
    }

    public Promise<D, F, P> progress(ProgressCallback<P> callback) {
        progressCallbacks.add(callback);
        return this;
    }

    public Promise<D, F, P> then(DoneCallback<D> callback) {
        this.done(callback);
        return this;
    }

    public Promise<D, F, P> then(DoneCallback<D> doneCallback, FailCallback<F> failCallback) {
        this.done(doneCallback);
        this.fail(failCallback);
        return this;
    }

    public Promise<D, F, P> then(DoneCallback<D> doneCallback, FailCallback<F> failCallback, ProgressCallback<P> progressCallback) {
        this.done(doneCallback);
        this.fail(failCallback);
        this.progress(progressCallback);
        return this;
    }

    public boolean isPending() {
        return this.state == State.PENDING;
    }

    public boolean isResolved() {
        return this.state == State.RESOLVED;
    }

    public boolean isRejected() {
        return this.state == State.REJECTED;
    }
}
