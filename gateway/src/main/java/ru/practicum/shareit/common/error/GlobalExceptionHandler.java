package ru.practicum.shareit.common.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Global exception handler for Gateway.
 * Handles request validation errors (400) and generic fallback (500).
 * Domain errors (403, 404, 409, etc.) are returned by the server and just proxied.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private ErrorResponse body(HttpStatus status, String msg, HttpServletRequest req) {
        return new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                StringUtils.hasText(msg) ? msg : status.getReasonPhrase(),
                req.getRequestURI()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " +
                        (StringUtils.hasText(fe.getDefaultMessage()) ? fe.getDefaultMessage() : "invalid"))
                .collect(Collectors.joining("; "));
        log.warn("400 {} {} -> {}", req.getMethod(), req.getRequestURI(), message);
        return body(HttpStatus.BAD_REQUEST, message, req);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        String message = ex.getConstraintViolations().stream()
                .map(this::formatViolation)
                .collect(Collectors.joining("; "));
        log.warn("400 {} {} -> {}", req.getMethod(), req.getRequestURI(), message);
        return body(HttpStatus.BAD_REQUEST, message, req);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest req) {
        String message = "Missing parameter: " + ex.getParameterName();
        log.warn("400 {} {} -> {}", req.getMethod(), req.getRequestURI(), message);
        return body(HttpStatus.BAD_REQUEST, message, req);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingHeader(MissingRequestHeaderException ex, HttpServletRequest req) {
        String message = "Missing header: " + ex.getHeaderName();
        log.warn("400 {} {} -> {}", req.getMethod(), req.getRequestURI(), message);
        return body(HttpStatus.BAD_REQUEST, message, req);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        String name = ex.getName();
        String message = StringUtils.hasText(name) ? "Invalid parameter '" + name + "'" : "Invalid parameter/type";
        log.warn("400 {} {} -> {}", req.getMethod(), req.getRequestURI(), message);
        return body(HttpStatus.BAD_REQUEST, message, req);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        String message = "Malformed JSON request body";
        log.warn("400 {} {} -> {}", req.getMethod(), req.getRequestURI(), message);
        return body(HttpStatus.BAD_REQUEST, message, req);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        String message = StringUtils.hasText(ex.getMessage()) ? ex.getMessage() : "Bad request";
        log.warn("400 {} {} -> {}", req.getMethod(), req.getRequestURI(), message);
        return body(HttpStatus.BAD_REQUEST, message, req);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleOther(Exception ex, HttpServletRequest req) {
        log.error("500 {} {} -> {}", req.getMethod(), req.getRequestURI(), ex.toString());
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", req);
    }

    private String formatViolation(ConstraintViolation<?> v) {
        String path = v.getPropertyPath() != null ? v.getPropertyPath().toString() : "param";
        String msg = StringUtils.hasText(v.getMessage()) ? v.getMessage() : "invalid";
        return path + ": " + msg;
    }
}