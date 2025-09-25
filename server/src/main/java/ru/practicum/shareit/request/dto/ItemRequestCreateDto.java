package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Request payload for creating an item request.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequestCreateDto {
        @NotBlank
        @Size(max = 500)
        private String description;
        // getters / ctor как у тебя
}