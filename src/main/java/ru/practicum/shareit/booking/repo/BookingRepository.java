package ru.practicum.shareit.booking.repo;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;


/**
 * Booking JPA repository.
 * Prefer derived query methods; keep JPQL only for complex cases (overlap, batch fetch).
 */

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Booker listings
    List<Booking> findByBooker_IdOrderByStartDesc(Long bookerId, Pageable pageable);

    List<Booking> findByBooker_IdAndStartBeforeAndEndAfterOrderByStartDesc(
            Long bookerId, Instant now1, Instant now2, Pageable pageable);

    List<Booking> findByBooker_IdAndEndBeforeOrderByStartDesc(
            Long bookerId, Instant before, Pageable pageable);

    List<Booking> findByBooker_IdAndStartAfterOrderByStartDesc(
            Long bookerId, Instant after, Pageable pageable);

    List<Booking> findByBooker_IdAndStatusOrderByStartDesc(
            Long bookerId, BookingStatus status, Pageable pageable);

    // Owner listings
    List<Booking> findByItem_Owner_IdOrderByStartDesc(Long ownerId, Pageable pageable);

    List<Booking> findByItem_Owner_IdAndStartBeforeAndEndAfterOrderByStartDesc(
            Long ownerId, Instant now1, Instant now2, Pageable pageable);

    List<Booking> findByItem_Owner_IdAndEndBeforeOrderByStartDesc(
            Long ownerId, Instant before, Pageable pageable);

    List<Booking> findByItem_Owner_IdAndStartAfterOrderByStartDesc(
            Long ownerId, Instant after, Pageable pageable);

    List<Booking> findByItem_Owner_IdAndStatusOrderByStartDesc(
            Long ownerId, BookingStatus status, Pageable pageable);

    // Comment eligibility
    boolean existsByBooker_IdAndItem_IdAndEndBeforeAndStatus(
            Long bookerId, Long itemId, Instant before, BookingStatus status);

    Optional<Booking> findTopByBooker_IdAndItem_IdAndStatusOrderByEndDesc(
            Long bookerId, Long itemId, BookingStatus status);

    @Query("""
   select (count(b) > 0)
   from Booking b
   where b.booker.id = :bookerId
     and b.item.id   = :itemId
     and b.status    = ru.practicum.shareit.booking.model.BookingStatus.APPROVED
     and b.end      <= :moment
   """)
    boolean hasFinishedApprovedBooking(@Param("bookerId") Long bookerId,
                                       @Param("itemId")   Long itemId,
                                       @Param("moment")   Instant moment);

    // Last/Next for item
    Optional<Booking> findTopByItem_IdAndStatusAndStartBeforeOrderByStartDesc(
            Long itemId, BookingStatus status, Instant before);

    Optional<Booking> findTopByItem_IdAndStatusAndStartAfterOrderByStartAsc(
            Long itemId, BookingStatus status, Instant after);

    // Batch last/next for multiple items
    @Query("""
        select b from Booking b
        where b.item.id in :itemIds
          and b.status = :status
          and b.start < :now
        order by b.start desc
        """)
    List<Booking> findAllLastForItems(@Param("itemIds") Collection<Long> itemIds,
                                      @Param("status") BookingStatus status,
                                      @Param("now") Instant now);

    @Query("""
        select b from Booking b
        where b.item.id in :itemIds
          and b.status = :status
          and b.start > :now
        order by b.start asc
        """)
    List<Booking> findAllNextForItems(@Param("itemIds") Collection<Long> itemIds,
                                      @Param("status") BookingStatus status,
                                      @Param("now") Instant now);

    // Overlap check for create
    @Query("""
        select (count(b) > 0)
        from Booking b
        where b.item.id = :itemId
          and b.status in :statuses
          and b.start < :end
          and b.end   > :start
        """)
    boolean hasOverlap(@Param("itemId") Long itemId,
                       @Param("statuses") Collection<BookingStatus> statuses,
                       @Param("start") Instant start,
                       @Param("end") Instant end);
}