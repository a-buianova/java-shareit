package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repo.BookingRepository;
import ru.practicum.shareit.common.exception.BadRequestException;
import ru.practicum.shareit.common.exception.ForbiddenException;
import ru.practicum.shareit.common.exception.NotFoundException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repo.CommentRepository;
import ru.practicum.shareit.item.repo.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repo.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepo;
    private final UserRepository userRepo;
    private final BookingRepository bookingRepo;
    private final CommentRepository commentRepo;
    private final ItemMapper mapper;
    private final ItemRequestRepository requestRepo;

    @Override
    @Transactional
    public ItemResponse create(Long ownerId, ItemCreateDto dto) {
        User owner = userRepo.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("owner not found"));

        ItemRequest request = null;
        if (dto.requestId() != null) {
            request = requestRepo.findById(dto.requestId())
                    .orElseThrow(() -> new NotFoundException("request not found"));
        }

        Item saved = itemRepo.save(mapper.toEntity(dto, owner, request));
        return mapper.toResponse(saved);
    }

    @Override
    public ItemDetailsResponse get(Long requesterId, Long itemId) {
        Item item = itemRepo.findById(itemId)
                .orElseThrow(() -> new NotFoundException("item not found"));

        List<Comment> comments = commentRepo.findByItem_IdOrderByCreatedAsc(itemId);

        boolean isOwner = item.getOwner() != null && Objects.equals(item.getOwner().getId(), requesterId);

        Booking last = null;
        Booking next = null;
        if (isOwner) {
            Instant now = Instant.now();
            last = bookingRepo.findTopByItem_IdAndStatusAndStartBeforeOrderByStartDesc(
                    itemId, BookingStatus.APPROVED, now).orElse(null);
            next = bookingRepo.findTopByItem_IdAndStatusAndStartAfterOrderByStartAsc(
                    itemId, BookingStatus.APPROVED, now).orElse(null);
        }

        return mapper.toDetails(item, last, next, comments);
    }

    @Override
    public List<ItemDetailsResponse> listOwnerItems(Long ownerId) {
        if (!userRepo.existsById(ownerId)) {
            throw new NotFoundException("user not found");
        }

        List<Item> items = itemRepo.findAllByOwner_IdOrderByIdAsc(ownerId);
        if (items.isEmpty()) {
            return List.of();
        }

        List<Long> itemIds = items.stream().map(Item::getId).toList();
        List<Comment> allComments = commentRepo.findByItem_IdInOrderByCreatedAsc(itemIds);
        Map<Long, List<Comment>> commentsByItem =
                allComments.stream().collect(Collectors.groupingBy(c -> c.getItem().getId(), Collectors.toList()));

        Instant now = Instant.now();

        List<Booking> lastAll = bookingRepo.findAllLastForItems(itemIds, BookingStatus.APPROVED, now);
        Map<Long, Booking> lastByItem = new HashMap<>();
        for (Booking b : lastAll) {
            Long id = b.getItem().getId();
            lastByItem.putIfAbsent(id, b);
        }

        List<Booking> nextAll = bookingRepo.findAllNextForItems(itemIds, BookingStatus.APPROVED, now);
        Map<Long, Booking> nextByItem = new HashMap<>();
        for (Booking b : nextAll) {
            Long id = b.getItem().getId();
            nextByItem.putIfAbsent(id, b);
        }

        List<ItemDetailsResponse> out = new ArrayList<>(items.size());
        for (Item it : items) {
            Long id = it.getId();
            Booking last = lastByItem.get(id);
            Booking next = nextByItem.get(id);
            List<Comment> comments = commentsByItem.getOrDefault(id, List.of());
            out.add(mapper.toDetails(it, last, next, comments));
        }
        return out;
    }

    @Override
    @Transactional
    public ItemResponse patch(Long ownerId, Long itemId, ItemUpdateDto dto) {
        Item existing = itemRepo.findById(itemId)
                .orElseThrow(() -> new NotFoundException("item not found"));

        if (existing.getOwner() == null || !existing.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("forbidden: not an owner");
        }
        mapper.patch(existing, dto);
        Item updated = itemRepo.save(existing);
        return mapper.toResponse(updated);
    }

    @Override
    public List<ItemResponse> search(String text) {
        if (text == null || text.isBlank()) return List.of();
        return itemRepo.searchAvailable(text.trim())
                .stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional
    public CommentResponse addComment(Long userId, Long itemId, CommentCreateDto dto) {
        User author = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("user not found"));
        Item item = itemRepo.findById(itemId)
                .orElseThrow(() -> new NotFoundException("item not found"));

        boolean allowed = bookingRepo.hasFinishedApprovedBooking(
                userId, itemId, Instant.now().plusSeconds(1));        if (!allowed) {
            throw new BadRequestException("user has not completed an approved booking of this item");
        }

        Comment saved = commentRepo.save(CommentMapper.toEntity(dto, item, author));
        return CommentMapper.toResponse(saved);
    }
}