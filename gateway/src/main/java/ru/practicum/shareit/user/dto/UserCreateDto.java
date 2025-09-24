package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class UserCreateDto {
    @NotBlank
    @Size(max = 255)
    String name;

    @NotBlank
    @Email
    @Size(max = 512)
    String email;
}