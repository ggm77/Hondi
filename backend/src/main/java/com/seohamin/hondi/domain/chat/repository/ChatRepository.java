package com.seohamin.hondi.domain.chat.repository;

import com.seohamin.hondi.domain.chat.entity.Chat;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    //마지막 메세지의 아이디 기반으로 그 이후 메세지 가져오기
    @Query("""
        SELECT c FROM Chat c
        LEFT JOIN FETCH c.sender
        WHERE c.ride.id = :rideId AND c.id > :lastChatId
        ORDER BY c.id ASC
    """)
    List<Chat> findAfter(
            @Param("rideId") Long rideId,
            @Param("lastChatId") Long lastChatId,
            Pageable pageable
    );

    //채팅방의 마지막 메세지
    Optional<Chat> findTopByRideIdOrderByIdDesc(Long rideId);
}
