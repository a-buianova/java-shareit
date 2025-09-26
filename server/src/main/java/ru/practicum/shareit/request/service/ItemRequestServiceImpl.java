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
import java.util.Map;
import java.util.stream.Collectors;

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
        int safeSize = size > 0 ? size : 10;
        int pageIdx = Math.max(0, from / safeSize);
        PageRequest page = PageRequest.of(pageIdx, safeSize);

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
        if (requests.isEmpty()) return List.of();

        List<Long> reqIds = requests.stream().map(ItemRequest::getId).toList();

        List<Item> allItems = itemRepo.findAllByRequest_IdInOrderByIdAsc(reqIds);

        Map<Long, List<Item>> byReqId = allItems.stream()
                .collect(Collectors.groupingBy(i -> i.getRequest().getId()));

        return requests.stream()
                .map(r -> mapper.toResponse(r, byReqId.getOrDefault(r.getId(), List.of())))
                .toList();
    }

    private void ensureUserExists(Long userId) {
        if (!userRepo.existsById(userId)) {
            throw new NotFoundException("user not found");
        }
    }
}