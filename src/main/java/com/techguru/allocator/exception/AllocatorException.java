package com.techguru.allocator.exception;

public class AllocatorException extends Exception {

    public AllocatorException(String message) {
        super(message);
    }

    public AllocatorException(String message, Throwable e) {
        super(message, e);
    }

}
