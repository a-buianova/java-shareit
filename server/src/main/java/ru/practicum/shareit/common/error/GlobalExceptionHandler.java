package ru.practicum.shareit.common.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.common.exception.BadRequestException;
import ru.practicum.shareit.common.exception.ConflictException;
import ru.practicum.shareit.common.exception.ForbiddenException;
import ru.practicum.shareit.common.exception.NotFoundException;

import java.time.Instant;
import java.util.NoSuchElementException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private ErrorResponse body(HttpStatus status, String msg, HttpServletRequest req) {
        return new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                msg,
                req.getRequestURI()
        );
    }

    // ---- 400: Bad Request (domain error) ----

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequest(BadRequestException ex, HttpServletRequest req) {
        log.warn("400 {} {} -> {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return body(HttpStatus.BAD_REQUEST, safeMsg(ex.getMessage(), "Bad request"), req);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        String msg = safeMsg(ex.getMessage(), "Bad request");
        log.warn("400 {} {} -> {}", req.getMethod(), req.getRequestURI(), msg);
        return body(HttpStatus.BAD_REQUEST, msg, req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + (fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid"))
                .collect(java.util.stream.Collectors.joining("; "));
        log.warn("400 {} {} -> {}", req.getMethod(), req.getRequestURI(), msg);
        return body(HttpStatus.BAD_REQUEST, (msg.isBlank() ? "Validation failed" : msg), req);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        String msg = ex.getConstraintViolations().stream()
                .map(v -> {
                    String path = (v.getPropertyPath() != null) ? v.getPropertyPath().toString() : "param";
                    String m = (v.getMessage() != null && !v.getMessage().isBlank()) ? v.getMessage() : "invalid";
                    return path + ": " + m;
                })
                .collect(java.util.stream.Collectors.joining("; "));
        log.warn("400 {} {} -> {}", req.getMethod(), req.getRequestURI(), msg);
        return body(HttpStatus.BAD_REQUEST, (msg.isBlank() ? "Validation failed" : msg), req);
    }

    // ---- 403: Forbidden ----

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleForbidden(ForbiddenException ex, HttpServletRequest req) {
        log.warn("403 {} {} -> {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return body(HttpStatus.FORBIDDEN, safeMsg(ex.getMessage(), "Forbidden"), req);
    }

    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleSecurity(SecurityException ex, HttpServletRequest req) {
        log.warn("403 {} {} -> {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return body(HttpStatus.FORBIDDEN, safeMsg(ex.getMessage(), "Forbidden"), req);
    }

    // ---- 404: Not Found ----

    @ExceptionHandler({ NotFoundException.class, NoSuchElementException.class })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(RuntimeException ex, HttpServletRequest req) {
        log.warn("404 {} {} -> {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return body(HttpStatus.NOT_FOUND, safeMsg(ex.getMessage(), "Resource not found"), req);
    }

    // ---- 409: Conflict ----

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflict(ConflictException ex, HttpServletRequest req) {
        log.warn("409 {} {} -> {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return body(HttpStatus.CONFLICT, safeMsg(ex.getMessage(), "Conflict"), req);
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleIllegalState(IllegalStateException ex, HttpServletRequest req) {
        log.warn("409 {} {} -> {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return body(HttpStatus.CONFLICT, safeMsg(ex.getMessage(), "Conflict"), req);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingHeader(MissingRequestHeaderException ex, HttpServletRequest req) {
        String message = "Missing header: " + ex.getHeaderName();
        log.warn("400 {} {} -> {}", req.getMethod(), req.getRequestURI(), message);
        return body(HttpStatus.BAD_REQUEST, message, req);
    }

    // ---- 500: Fallback ----

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleOther(Exception ex, HttpServletRequest req) {
        log.error("500 {} {} -> {}", req.getMethod(), req.getRequestURI(), ex.toString());
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", req);
    }

    private String safeMsg(String msg, String fallback) {
        return StringUtils.hasText(msg) ? msg : fallback;
    }
}