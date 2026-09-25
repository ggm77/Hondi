package com.seohamin.hondi.domain.ride.repository.participant;

import com.seohamin.hondi.domain.ride.entity.participant.RideParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RideParticipantRepository extends JpaRepository<RideParticipant, Long> {

    Optional<RideParticipant> findByRideIdAndUserId(Long rideId, Long userId);

    boolean existsByRideIdAndUserId(Long rideId, Long userId);

    void deleteByRideId(Long rideId);

    //현재 목록에서 참여 중인 모집글 ID를 한 번에 조회
    @Query("SELECT p.ride.id FROM RideParticipant p WHERE p.user.id = :userId AND p.ride.id IN :rideIds")
    Set<Long> findJoinedRideIds(@Param("userId") Long userId, @Param("rideIds") List<Long> rideIds);

    //모집글의 참여자들을 유저 정보와 같이 조회
    @Query("""
        SELECT p FROM RideParticipant p
        JOIN FETCH p.user
        WHERE p.ride.id = :rideId
        ORDER BY p.id ASC
    """)
    List<RideParticipant> findByRideIdWithUser(@Param("rideId") Long rideId);

    //내가 참여한 모집글
    @Query("""
        SELECT p FROM RideParticipant p
        JOIN FETCH p.ride r
        JOIN FETCH r.host
        WHERE p.user.id = :userId
        ORDER BY r.departureAt DESC
    """)
    List<RideParticipant> findByUserIdWithRide(@Param("userId") Long userId);
}
