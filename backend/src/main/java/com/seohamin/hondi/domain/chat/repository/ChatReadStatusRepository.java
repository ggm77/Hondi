package com.seohamin.hondi.domain.chat.repository;

import com.seohamin.hondi.domain.chat.entity.ChatReadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatReadStatusRepository extends JpaRepository<ChatReadStatus, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM ChatReadStatus s WHERE s.ride.id = :rideId")
    void deleteByRideId(@Param("rideId") Long rideId);

    Optional<ChatReadStatus> findByRideIdAndUserId(Long rideId, Long userId);
}
