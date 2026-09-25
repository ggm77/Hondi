package com.seohamin.hondi.domain.ride.repository;

import com.seohamin.hondi.domain.ride.entity.Ride;
import com.seohamin.hondi.domain.ride.entity.RideStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RideRepository extends JpaRepository<Ride, Long> {

    //정원 안에서만 인원을 늘리는 원자적 UPDATE (락 대신 DB 조건으로 정원 보장)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE Ride r SET r.currentCount = r.currentCount + 1
        WHERE r.id = :id AND r.status = :status AND r.currentCount < r.capacity
    """)
    int increaseCountIfAvailable(@Param("id") Long id, @Param("status") RideStatus status);

    //방장 혼자 남았을 때는 더 줄지 않는 원자적 UPDATE
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Ride r SET r.currentCount = r.currentCount - 1 WHERE r.id = :id AND r.currentCount > 1")
    int decreaseCount(@Param("id") Long id);

    //출발지 바운딩 박스와 출발 시간 범위로 매칭 후보 조회 (정원 다 찬 글 제외)
    @Query("""
        SELECT r FROM Ride r
        JOIN FETCH r.host
        WHERE r.status = :status
          AND r.currentCount < r.capacity
          AND r.departureAt BETWEEN :fromAt AND :toAt
          AND r.originLat BETWEEN :minLat AND :maxLat
          AND r.originLon BETWEEN :minLon AND :maxLon
    """)
    List<Ride> findMatchCandidates(
            @Param("status") RideStatus status,
            @Param("fromAt") LocalDateTime fromAt,
            @Param("toAt") LocalDateTime toAt,
            @Param("minLat") BigDecimal minLat,
            @Param("maxLat") BigDecimal maxLat,
            @Param("minLon") BigDecimal minLon,
            @Param("maxLon") BigDecimal maxLon
    );

    //출발 예정인 모집 중인 글을 출발 시간 순으로 조회 (정원 다 찬 글 제외)
    @Query(
            value = """
                SELECT r FROM Ride r
                JOIN FETCH r.host
                WHERE r.status = :status AND r.currentCount < r.capacity AND r.departureAt > :now
                ORDER BY r.departureAt ASC
            """,
            countQuery = "SELECT COUNT(r) FROM Ride r WHERE r.status = :status AND r.currentCount < r.capacity AND r.departureAt > :now"
    )
    Slice<Ride> findUpcoming(
            @Param("status") RideStatus status,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    //내가 방장인 글
    @Query("SELECT r FROM Ride r JOIN FETCH r.host WHERE r.host.id = :hostId ORDER BY r.departureAt DESC")
    List<Ride> findByHostId(@Param("hostId") Long hostId);
}
