package com.example.ces.exception;

public class CourseFullException extends RuntimeException {
    public CourseFullException (String message) {
        super(message);
    }
}
