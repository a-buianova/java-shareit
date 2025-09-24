package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.common.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repo.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponse;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repo.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository requestRepo;
    private final ItemRepository itemRepo;
    private final UserRepository userRepo;
    private final ItemRequestMapper mapper;

    @Override
    @Transactional
    public ItemRequestResponse create(Long userId, ItemRequestCreateDto dto) {
        User requestor = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("user not found"));

        ItemRequest saved = requestRepo.save(mapper.toEntity(dto, requestor));
        return mapper.toResponse(saved, Collections.emptyList());
    }

    @Override
    public List<ItemRequestResponse> findOwn(Long userId) {
        ensureUserExists(userId);
        List<ItemRequest> requests = requestRepo.findByRequestor_IdOrderByCreatedDesc(userId);
        return attachItems(requests);
    }

    @Override
    public List<ItemRequestResponse> findAllExceptUser(Long userId, int from, int size) {
        ensureUserExists(userId);
        PageRequest page = PageRequest.of(from / size, size);
        List<ItemRequest> requests = requestRepo.findByRequestor_IdNotOrderByCreatedDesc(userId, page);
        return attachItems(requests);
    }

    @Override
    public ItemRequestResponse getById(Long userId, Long requestId) {
        ensureUserExists(userId);
        ItemRequest req = requestRepo.findById(requestId)
                .orElseThrow(() -> new NotFoundException("request not found"));

        List<Item> items = itemRepo.findAllByRequest_IdOrderByIdAsc(req.getId());
        return mapper.toResponse(req, items);
    }

    private List<ItemRequestResponse> attachItems(List<ItemRequest> requests) {
        return requests.stream()
                .map(r -> mapper.toResponse(
                        r,
                        itemRepo.findAllByRequest_IdOrderByIdAsc(r.getId())
                ))
                .toList();
    }

    private void ensureUserExists(Long userId) {
        if (!userRepo.existsById(userId)) {
            throw new NotFoundException("user not found");
        }
    }
}