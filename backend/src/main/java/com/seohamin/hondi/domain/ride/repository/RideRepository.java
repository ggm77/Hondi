package com.seohamin.hondi.domain.ride.repository;

import com.seohamin.hondi.domain.ride.entity.Ride;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RideRepository extends JpaRepository<Ride, Long> {

    //모집글과 참여/채팅 정보를 변경할 때 먼저 잠그고 트랜잭션 종료까지 유지
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Ride r WHERE r.id = :id")
    Optional<Ride> findByIdForUpdate(@Param("id") Long id);

    //정원 안에서만 인원을 늘리는 원자적 UPDATE
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE Ride r SET r.currentCount = r.currentCount + 1
        WHERE r.id = :id AND r.currentCount < r.capacity
    """)
    int increaseCountIfAvailable(@Param("id") Long id);

    //방장 혼자 남았을 때는 더 줄지 않는 원자적 UPDATE
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Ride r SET r.currentCount = r.currentCount - 1 WHERE r.id = :id AND r.currentCount > 1")
    int decreaseCount(@Param("id") Long id);

    //출발지 바운딩 박스와 출발 시간 범위로 매칭 후보 조회 (정원 다 찬 글 제외)
    @Query("""
        SELECT r FROM Ride r
        JOIN FETCH r.host
        WHERE r.currentCount < r.capacity
          AND r.departureAt BETWEEN :fromAt AND :toAt
          AND r.originLat BETWEEN :minLat AND :maxLat
          AND r.originLon BETWEEN :minLon AND :maxLon
          AND NOT EXISTS (
              SELECT p.id FROM RideParticipant p
              WHERE p.ride = r AND p.user.id = :userId
          )
    """)
    List<Ride> findMatchCandidates(
            @Param("fromAt") Instant fromAt,
            @Param("toAt") Instant toAt,
            @Param("minLat") BigDecimal minLat,
            @Param("maxLat") BigDecimal maxLat,
            @Param("minLon") BigDecimal minLon,
            @Param("maxLon") BigDecimal maxLon,
            @Param("userId") Long userId
    );

    //출발 예정인 모집 중인 글을 출발 시간 순으로 조회 (정원 다 찬 글 제외)
    @Query(
            value = """
                SELECT r FROM Ride r
                JOIN FETCH r.host
                WHERE r.currentCount < r.capacity AND r.departureAt > :now
                ORDER BY r.departureAt ASC
            """,
            countQuery = "SELECT COUNT(r) FROM Ride r WHERE r.currentCount < r.capacity AND r.departureAt > :now"
    )
    Slice<Ride> findUpcoming(
            @Param("now") Instant now,
            Pageable pageable
    );

    //내가 방장인 글
    @Query("SELECT r FROM Ride r JOIN FETCH r.host WHERE r.host.id = :hostId ORDER BY r.departureAt DESC")
    List<Ride> findByHostId(@Param("hostId") Long hostId);
}
