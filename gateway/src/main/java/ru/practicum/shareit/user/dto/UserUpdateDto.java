package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Value;

/** Partial update: any field may be null (ignored by server). */
@Value
public class UserUpdateDto {
    @Size(max = 255)
    String name;

    @Email
    @Size(max = 512)
    String email;
}