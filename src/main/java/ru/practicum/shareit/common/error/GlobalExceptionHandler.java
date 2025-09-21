package ru.practicum.shareit.common.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import ru.practicum.shareit.common.exception.BadRequestException;
import ru.practicum.shareit.common.exception.ConflictException;
import ru.practicum.shareit.common.exception.ForbiddenException;
import ru.practicum.shareit.common.exception.NotFoundException;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * @apiNote Converts exceptions into deterministic HTTP responses with a unified body.
 * @implNote
 * 400 – validation/invalid input;
 * 403 – forbidden (permission/ownership);
 * 404 – not found;
 * 409 – conflict (domain uniqueness);
 * 500 – unexpected errors.
 */
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

    // ---- 400: Bad Request ----

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequest(BadRequestException ex, HttpServletRequest req) {
        log.warn("400 {} {} -> {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return body(HttpStatus.BAD_REQUEST, safeMsg(ex.getMessage(), "Bad request"), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + (StringUtils.hasText(fe.getDefaultMessage()) ? fe.getDefaultMessage() : "invalid"))
                .collect(Collectors.joining("; "));
        if (!StringUtils.hasText(message)) message = "Validation failed";
        log.warn("400 {} {} -> {}", req.getMethod(), req.getRequestURI(), message);
        return body(HttpStatus.BAD_REQUEST, message, req);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        String message = ex.getConstraintViolations().stream()
                .map(this::formatViolation)
                .collect(Collectors.joining("; "));
        if (!StringUtils.hasText(message)) message = "Validation failed";
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

    @ExceptionHandler({ MethodArgumentTypeMismatchException.class, ConversionFailedException.class })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleTypeMismatch(Exception ex, HttpServletRequest req) {
        String message = "Invalid parameter/type";
        if (ex instanceof MethodArgumentTypeMismatchException matme && matme.getName() != null) {
            message = "Invalid parameter '" + matme.getName() + "'";
        }
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
        String message = safeMsg(ex.getMessage(), "Invalid parameter");
        log.warn("400 {} {} -> {}", req.getMethod(), req.getRequestURI(), message);
        return body(HttpStatus.BAD_REQUEST, message, req);
    }

    // ---- 405 Method Not Allowed ----
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ErrorResponse handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        log.warn("405 {} {} -> {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return body(HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed", req);
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

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNoResource(NoResourceFoundException ex, HttpServletRequest req) {
        log.warn("404 {} {} -> No handler for request", req.getMethod(), req.getRequestURI());
        return body(HttpStatus.NOT_FOUND, "No handler for request", req);
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

    // ---- 500: Fallback ----

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleOther(Exception ex, HttpServletRequest req) {
        log.error("500 {} {} -> {}", req.getMethod(), req.getRequestURI(), ex.toString());
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", req);
    }

    // ---- helpers ----

    private String formatViolation(ConstraintViolation<?> v) {
        String path = v.getPropertyPath() != null ? v.getPropertyPath().toString() : "param";
        String msg = StringUtils.hasText(v.getMessage()) ? v.getMessage() : "invalid";
        return path + ": " + msg;
    }

    private String safeMsg(String msg, String fallback) {
        return StringUtils.hasText(msg) ? msg : fallback;
    }

}