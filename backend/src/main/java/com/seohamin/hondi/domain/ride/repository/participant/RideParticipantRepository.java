package com.seohamin.hondi.domain.ride.repository.participant;

import com.seohamin.hondi.domain.ride.entity.participant.ParticipantStatus;
import com.seohamin.hondi.domain.ride.entity.participant.RideParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RideParticipantRepository extends JpaRepository<RideParticipant, Long> {

    Optional<RideParticipant> findByRideIdAndUserId(Long rideId, Long userId);

    //모집글의 참여자들을 유저 정보와 같이 조회
    @Query("""
        SELECT p FROM RideParticipant p
        JOIN FETCH p.user
        WHERE p.ride.id = :rideId AND p.status IN :statuses
        ORDER BY p.id ASC
    """)
    List<RideParticipant> findByRideIdAndStatusIn(
            @Param("rideId") Long rideId,
            @Param("statuses") Collection<ParticipantStatus> statuses
    );

    //내가 참여한 모집글
    @Query("""
        SELECT p FROM RideParticipant p
        JOIN FETCH p.ride r
        JOIN FETCH r.host
        WHERE p.user.id = :userId AND p.status IN :statuses
        ORDER BY r.departureAt DESC
    """)
    List<RideParticipant> findByUserIdAndStatusIn(
            @Param("userId") Long userId,
            @Param("statuses") Collection<ParticipantStatus> statuses
    );
}
