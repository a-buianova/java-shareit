package ru.practicum.shareit.request.dto;

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
        private String description;
}