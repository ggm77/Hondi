package com.seohamin.hondi.domain.chat.repository;

import com.seohamin.hondi.domain.chat.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM ChatMessage m WHERE m.ride.id = :rideId")
    void deleteByRideId(@Param("rideId") Long rideId);

    boolean existsByIdAndRideId(Long id, Long rideId);

    Optional<ChatMessage> findTopByRideIdOrderByIdDesc(Long rideId);

    long countByRideIdAndIdGreaterThan(Long rideId, Long id);

    //폴링용 - id보다 큰 메시지를 오래된 순으로
    @Query("""
        SELECT m FROM ChatMessage m
        JOIN FETCH m.sender
        WHERE m.ride.id = :rideId AND m.id > :afterId
        ORDER BY m.id ASC
    """)
    List<ChatMessage> findAfter(@Param("rideId") Long rideId, @Param("afterId") Long afterId, Pageable pageable);

    //과거 메시지 스크롤용 - id보다 작은 메시지를 최신 순으로
    @Query("""
        SELECT m FROM ChatMessage m
        JOIN FETCH m.sender
        WHERE m.ride.id = :rideId AND m.id < :beforeId
        ORDER BY m.id DESC
    """)
    List<ChatMessage> findBefore(@Param("rideId") Long rideId, @Param("beforeId") Long beforeId, Pageable pageable);

    //커서 없는 최초 조회용 - 최신 메시지부터
    @Query("""
        SELECT m FROM ChatMessage m
        JOIN FETCH m.sender
        WHERE m.ride.id = :rideId
        ORDER BY m.id DESC
    """)
    List<ChatMessage> findLatest(@Param("rideId") Long rideId, Pageable pageable);
}
