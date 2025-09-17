package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserResponse;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import java.util.List;

/** Use-case API for users. */
public interface UserService {

    UserResponse create(UserCreateDto dto);

    UserResponse get(Long id);

    List<UserResponse> list();

    UserResponse patch(Long id, UserUpdateDto dto);

    void delete(Long id);
}