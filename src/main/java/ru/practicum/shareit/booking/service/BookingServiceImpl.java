package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.booking.dto.BookingStateParam;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repo.BookingRepository;
import ru.practicum.shareit.common.exception.BadRequestException;
import ru.practicum.shareit.common.exception.ForbiddenException;   // ← добавили
import ru.practicum.shareit.common.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repo.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepo;
    private final ItemRepository itemRepo;
    private final UserRepository userRepo;

    @Override
    @Transactional
    public BookingResponse create(Long userId, BookingCreateDto dto) {
        User booker = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("user not found"));
        Item item = itemRepo.findById(dto.itemId())
                .orElseThrow(() -> new NotFoundException("item not found"));

        LocalDateTime start = dto.start();
        LocalDateTime end   = dto.end();
        if (start == null || end == null || !start.isBefore(end)) {
            throw new BadRequestException("invalid time window");
        }
        if (!item.isAvailable()) {
            throw new BadRequestException("item is not available");
        }
        if (item.getOwner() != null && item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("owner cannot book own item");
        }

        Booking saved = bookingRepo.save(BookingMapper.toEntity(dto, item, booker));
        return BookingMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public BookingResponse approve(Long ownerId, Long bookingId, boolean approved) {
        Booking b = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("booking not found"));

        if (!b.getItem().getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("only owner can approve");
        }
        if (b.getStatus() != BookingStatus.WAITING) {
            throw new BadRequestException("booking is not in WAITING state");
        }

        b.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return BookingMapper.toResponse(bookingRepo.save(b));
    }

    @Override
    public BookingResponse get(Long userId, Long bookingId) {
        Booking b = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("booking not found"));

        boolean isOwner = b.getItem().getOwner().getId().equals(userId);
        boolean isBooker = b.getBooker().getId().equals(userId);
        if (!isOwner && !isBooker) {
            throw new NotFoundException("no access to booking");
        }
        return BookingMapper.toResponse(b);
    }

    @Override
    public List<BookingResponse> listUser(Long userId, BookingStateParam state, int from, int size) {
        ensureUserExists(userId);
        var page = PageRequest.of(from / size, size);
        Instant now = Instant.now();

        List<Booking> data = switch (state) {
            case ALL      -> bookingRepo.findByBooker_IdOrderByStartDesc(userId, page);
            case CURRENT  -> bookingRepo.findByBooker_IdAndStartBeforeAndEndAfterOrderByStartDesc(userId, now, now, page);
            case PAST     -> bookingRepo.findByBooker_IdAndEndBeforeOrderByStartDesc(userId, now, page);
            case FUTURE   -> bookingRepo.findByBooker_IdAndStartAfterOrderByStartDesc(userId, now, page);
            case WAITING  -> bookingRepo.findByBooker_IdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING, page);
            case REJECTED -> bookingRepo.findByBooker_IdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED, page);
        };
        return data.stream().map(BookingMapper::toResponse).toList();
    }

    @Override
    public List<BookingResponse> listOwner(Long ownerId, BookingStateParam state, int from, int size) {
        ensureUserExists(ownerId);
        var page = PageRequest.of(from / size, size);
        Instant now = Instant.now();

        List<Booking> data = switch (state) {
            case ALL      -> bookingRepo.findAllByOwner(ownerId, page);
            case CURRENT  -> bookingRepo.findCurrentByOwner(ownerId, now, page);
            case PAST     -> bookingRepo.findPastByOwner(ownerId, now, page);
            case FUTURE   -> bookingRepo.findFutureByOwner(ownerId, now, page);
            case WAITING  -> bookingRepo.findByOwnerAndStatus(ownerId, BookingStatus.WAITING, page);
            case REJECTED -> bookingRepo.findByOwnerAndStatus(ownerId, BookingStatus.REJECTED, page);
        };
        return data.stream().map(BookingMapper::toResponse).toList();
    }

    private void ensureUserExists(Long userId) {
        if (!userRepo.existsById(userId)) {
            throw new NotFoundException("user not found");
        }
    }
}