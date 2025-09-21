package ru.practicum.shareit.common.web;

import java.lang.annotation.*;

/**
 * @apiNote Marks a controller method parameter as the current user id,
 * extracted from the HTTP header {@code X-Sharer-User-Id}.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUserId { }