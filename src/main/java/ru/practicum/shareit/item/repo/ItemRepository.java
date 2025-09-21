package ru.practicum.shareit.item.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.List;

/**
 * JPA repository for items.
 */
public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findAllByOwner_IdOrderByIdAsc(Long ownerId);

    List<Item> findAllByRequest_IdOrderByIdAsc(Long requestId);

    List<Item> findAllByRequest_IdInOrderByIdAsc(Collection<Long> requestIds);

    @Query("""
           select i
           from Item i
           where i.available = true
             and (
                  lower(i.name) like lower(concat('%', :q, '%'))
               or lower(i.description) like lower(concat('%', :q, '%'))
             )
           """)
    List<Item> searchAvailable(String q);
}