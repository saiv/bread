package it.saiv.bread.promises;

public interface ProgressCallback<P> {
    public void onProgress(final P progress);
}
