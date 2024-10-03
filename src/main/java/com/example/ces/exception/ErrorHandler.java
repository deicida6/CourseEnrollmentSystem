package com.example.ces.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestControllerAdvice
public class ErrorHandler {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleIllegalArgumentException(IllegalArgumentException ex) throws IOException {
        return buildApiError(Collections.singletonList(getStackTraceAsString(ex)),
                HttpStatus.BAD_REQUEST,
                "Некорректный запрос",
                ex.getLocalizedMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<String> errors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }
        return buildApiError(errors,
                HttpStatus.BAD_REQUEST,
                "Запрашиваемый объект не валиден",
                "Пожалуйста, проверьте корректность введенных данных");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleConstraintViolationException(ConstraintViolationException ex) {
        List<String> errors = new ArrayList<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            errors.add(violation.getMessage());
        }
        return buildApiError(errors,
                HttpStatus.BAD_REQUEST,
                "Запрашиваемый объект не валиден",
                "Пожалуйста, проверьте корректность введенных данных");
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(NotFoundException e) throws IOException {
        return buildApiError(Collections.singletonList(getStackTraceAsString(e)),
                HttpStatus.NOT_FOUND,
                "Запрашиваемый объект не найден",
                e.getLocalizedMessage());
    }

    @ExceptionHandler(CourseFullException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleCourseFullException(CourseFullException e) {
        return buildApiError(Collections.singletonList(e.getMessage()),
                HttpStatus.BAD_REQUEST,
                "Курс заполнен",
                "Студенты не могут записаться на курс, так как все места заняты");
    }

    @ExceptionHandler(WindowClosedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleWindowsClosedException(WindowClosedException e) {
        return buildApiError(Collections.singletonList(e.getMessage()),
                HttpStatus.BAD_REQUEST,
                "Окно записи закрыто",
                "Запись на курс закрыта, пожалуйста, попробуйте позже");
    }

    private ApiError buildApiError(List<String> errors, HttpStatus status, String reason, String message) {
        return ApiError.builder()
                .errors(errors)
                .status(status)
                .reason(reason)
                .message(message)
                .timestamp(LocalDateTime.now().format(DATE_TIME_FORMATTER))
                .build();
    }

    private String getStackTraceAsString(Exception e) throws IOException {
        try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
            e.printStackTrace(pw);
            return sw.toString();
        }
    }
}
