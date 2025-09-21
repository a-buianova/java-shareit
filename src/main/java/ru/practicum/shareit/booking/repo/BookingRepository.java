package ru.practicum.shareit.booking.repo;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBooker_IdOrderByStartDesc(Long bookerId, Pageable pageable);

    List<Booking> findByBooker_IdAndStartBeforeAndEndAfterOrderByStartDesc(
            Long bookerId, Instant now1, Instant now2, Pageable pageable);

    List<Booking> findByBooker_IdAndEndBeforeOrderByStartDesc(
            Long bookerId, Instant before, Pageable pageable);

    List<Booking> findByBooker_IdAndStartAfterOrderByStartDesc(
            Long bookerId, Instant after, Pageable pageable);

    List<Booking> findByBooker_IdAndStatusOrderByStartDesc(
            Long bookerId, BookingStatus status, Pageable pageable);

    @Query("""
            select b from Booking b
            where b.item.owner.id = :ownerId
            order by b.start desc
            """)
    List<Booking> findAllByOwner(Long ownerId, Pageable pageable);

    @Query("""
            select b from Booking b
            where b.item.owner.id = :ownerId
              and b.start < :now and b.end > :now
            order by b.start desc
            """)
    List<Booking> findCurrentByOwner(Long ownerId, Instant now, Pageable pageable);

    @Query("""
            select b from Booking b
            where b.item.owner.id = :ownerId
              and b.end < :before
            order by b.start desc
            """)
    List<Booking> findPastByOwner(Long ownerId, Instant before, Pageable pageable);

    @Query("""
            select b from Booking b
            where b.item.owner.id = :ownerId
              and b.start > :after
            order by b.start desc
            """)
    List<Booking> findFutureByOwner(Long ownerId, Instant after, Pageable pageable);

    @Query("""
            select b from Booking b
            where b.item.owner.id = :ownerId
              and b.status = :status
            order by b.start desc
            """)
    List<Booking> findByOwnerAndStatus(Long ownerId, BookingStatus status, Pageable pageable);

    @Query("""
       select (count(b) > 0)
       from Booking b
       where b.booker.id = :bookerId
         and b.item.id   = :itemId
         and b.status    = ru.practicum.shareit.booking.model.BookingStatus.APPROVED
         and b.end      <= :moment
       """)
    boolean hasFinishedApprovedBooking(Long bookerId, Long itemId, Instant moment);

    boolean existsByBooker_IdAndItem_IdAndEndBeforeAndStatus(
            Long bookerId, Long itemId, Instant before, BookingStatus status);

    Optional<Booking> findTopByItem_IdAndStatusAndStartBeforeOrderByStartDesc(
            Long itemId, BookingStatus status, Instant before);

    Optional<Booking> findTopByItem_IdAndStatusAndStartAfterOrderByStartAsc(
            Long itemId, BookingStatus status, Instant after);

    Optional<Booking> findTopByBooker_IdAndItem_IdAndStatusOrderByEndDesc(
            Long bookerId,
            Long itemId,
            BookingStatus status
    );
}