package ru.practicum.shareit.common.web;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import ru.practicum.shareit.common.exception.BadRequestException;

/**
 * @apiNote Resolves a {@code Long} parameter annotated with {@link CurrentUserId}
 *          from the header {@code X-Sharer-User-Id}.
 * @throws BadRequestException if the header is missing or invalid.
 */
@Component
public class UserIdArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String HEADER = "X-Sharer-User-Id";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUserId.class)
                && Long.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        String raw = webRequest.getHeader(HEADER);
        if (raw == null || raw.isBlank()) {
            throw new BadRequestException("Missing " + HEADER);
        }
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException ex) {
            throw new BadRequestException("Invalid " + HEADER + ": " + raw);
        }
    }
}