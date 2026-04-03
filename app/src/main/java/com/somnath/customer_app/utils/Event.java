package com.somnath.customer_app.utils;

/**
 * Used as a wrapper for data that is exposed via a LiveData that represents an event.
 * This ensures that the event is only consumed once, preventing issues with configuration changes
 * or multiple observers receiving the same event multiple times.
 */
public class Event<T> {
    private T content;
    private boolean hasBeenHandled = false;

    public Event(T content) {
        this.content = content;
    }

    /**
     * Returns the content and prevents its use again.
     */
    public T getContentIfNotHandled() {
        if (hasBeenHandled) {
            return null;
        } else {
            hasBeenHandled = true;
            return content;
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    public T peekContent() {
        return content;
    }

    public boolean hasBeenHandled() {
        return hasBeenHandled;
    }
}
