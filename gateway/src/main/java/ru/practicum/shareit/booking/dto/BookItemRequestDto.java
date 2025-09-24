package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookItemRequestDto {

	@Positive
	private long itemId;

	@NotNull
	@FutureOrPresent
	private LocalDateTime start;

	@NotNull
	@Future
	private LocalDateTime end;

	@AssertTrue(message = "start must be before end")
	public boolean isStartBeforeEnd() {
		if (start == null || end == null) return true;
		return end.isAfter(start);
	}
}