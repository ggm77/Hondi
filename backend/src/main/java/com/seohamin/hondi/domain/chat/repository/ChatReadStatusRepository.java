package com.seohamin.hondi.domain.chat.repository;

import com.seohamin.hondi.domain.chat.entity.ChatReadStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatReadStatusRepository extends JpaRepository<ChatReadStatus, Long> {

    Optional<ChatReadStatus> findByRideIdAndUserId(Long rideId, Long userId);
}
