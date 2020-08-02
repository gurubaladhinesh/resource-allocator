package com.techguru.allocator.exception;

/**
 * Central exception class for all exception thrown by the allocator
 */
public class AllocatorException extends Exception {

    /**
     * Instantiates a new Allocator exception.
     *
     * @param message the message
     */
    public AllocatorException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Allocator exception.
     *
     * @param message the message
     * @param e       the Throwable
     */
    public AllocatorException(String message, Throwable e) {
        super(message, e);
    }

}
