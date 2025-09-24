package ru.practicum.shareit.common.web;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import ru.practicum.shareit.common.exception.BadRequestException;

/**
 * Resolves a {@code long}/{@code Long} parameter annotated with {@link CurrentUserId}
 * from the HTTP header {@code X-Sharer-User-Id}.
 *
 * <p>Throws:
 * <ul>
 *   <li>{@link MissingRequestHeaderException} if header is absent/blank</li>
 *   <li>{@link BadRequestException} if header cannot be parsed as a number</li>
 * </ul>
 */
@Component
public class UserIdArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String HEADER = "X-Sharer-User-Id";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUserId.class)
                && (Long.class.equals(parameter.getParameterType())
                || long.class.equals(parameter.getParameterType()));
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {
        final String raw = webRequest.getHeader(HEADER);

        if (raw == null || raw.isBlank()) {
            throw new MissingRequestHeaderException(HEADER, parameter);
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException ex) {
            throw new BadRequestException("Invalid " + HEADER + ": " + raw);
        }
    }
}