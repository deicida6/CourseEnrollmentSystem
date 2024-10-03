package com.example.ces.exception;

public class WindowClosedException extends RuntimeException {
    public WindowClosedException (String message) {
        super(message);
    }
}
