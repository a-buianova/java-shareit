package ru.practicum.shareit.request.repo;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    List<ItemRequest> findByRequestor_IdOrderByCreatedDesc(Long requestorId);

    List<ItemRequest> findByRequestor_IdNotOrderByCreatedDesc(Long excludedRequestorId,
                                                              Pageable pageable);
}