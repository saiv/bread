package it.saiv.bread.promises;

public class DeferredObject<D, F, P> extends AbstractPromise<D, F, P> implements Deferred<D, F, P> {

    public Deferred<D, F, P> resolve(final D resolve) {
        if (!this.isPending()) {
            throw new IllegalStateException("Deferred object already finished, cannot resolve again");
        }

        this.state = State.RESOLVED;
        this.resolveResult = resolve;

        try {
            this.triggerDone(resolve);
        } finally {
            this.triggerAlways(this.state, resolve, null);
        }
        return this;
    }

    public Deferred<D, F, P> update(final P update) {
        if (!this.isPending()) {
            throw new IllegalStateException("Deferred object already finished, cannot update progress");
        }

        this.triggerProgress(update);
        return this;
    }

    public Deferred<D, F, P> reject(final F reject) {
        if (!this.isPending()) {
            throw new IllegalStateException("Deferred object already finished, cannot reject again");
        }
        this.state = State.REJECTED;
        this.rejectResult = reject;

        try {
            this.triggerFail(reject);
        } finally {
            this.triggerAlways(this.state, null, reject);
        }
        return this;
    }

    public Promise<D, F, P> promise() {
        return this;
    }
}
