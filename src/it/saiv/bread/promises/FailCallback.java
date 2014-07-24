package it.saiv.bread.promises;

public interface FailCallback<F> {
    public void onFail(final F result);
}
