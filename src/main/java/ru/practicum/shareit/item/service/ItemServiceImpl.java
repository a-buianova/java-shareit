package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.common.exception.ForbiddenException;
import ru.practicum.shareit.common.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemResponse;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repo.InMemoryItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.InMemoryUserRepository;

import java.util.List;

/**
 * @apiNote Default implementation with owner checks and search rules.
 */
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final InMemoryItemRepository repo;
    private final InMemoryUserRepository userRepo;
    private final ItemMapper mapper;

    @Override
    public ItemResponse create(Long ownerId, ItemCreateDto dto) {
        User owner = userRepo.findById(ownerId).orElseThrow(() -> new NotFoundException("owner not found"));
        Item entity = mapper.toEntity(dto, owner, null);
        Item saved = repo.save(entity);
        return mapper.toResponse(saved);
    }

    @Override
    public ItemResponse get(Long id) {
        Item item = repo.findById(id).orElseThrow(() -> new NotFoundException("item not found"));
        return mapper.toResponse(item);
    }

    @Override
    public List<ItemResponse> listOwnerItems(Long ownerId) {
        return repo.findByOwner(ownerId).stream().map(mapper::toResponse).toList();
    }

    @Override
    public ItemResponse patch(Long ownerId, Long itemId, ItemUpdateDto dto) {
        Item existing = repo.findById(itemId).orElseThrow(() -> new NotFoundException("item not found"));
        if (existing.getOwner() == null || !existing.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("forbidden: not an owner");
        }
        mapper.patch(existing, dto);
        Item updated = repo.update(existing).orElseThrow(() -> new NotFoundException("item not found"));
        return mapper.toResponse(updated);
    }

    @Override
    public List<ItemResponse> search(String text) {
        return repo.searchAvailable(text).stream().map(mapper::toResponse).toList();
    }
}