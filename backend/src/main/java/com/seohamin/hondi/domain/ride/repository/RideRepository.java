package com.seohamin.hondi.domain.ride.repository;

import com.seohamin.hondi.domain.ride.entity.Ride;
import com.seohamin.hondi.domain.ride.entity.RideStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RideRepository extends JpaRepository<Ride, Long> {

    //인원 변경시 동시성 문제 막기 위해 락 걸고 조회
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Ride r WHERE r.id = :id")
    Optional<Ride> findByIdForUpdate(@Param("id") Long id);

    //출발지 바운딩 박스와 출발 시간 범위로 매칭 후보 조회
    @Query("""
        SELECT r FROM Ride r
        JOIN FETCH r.host
        WHERE r.status = :status
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

    //출발 예정인 모집 중인 글을 출발 시간 순으로 조회
    @Query(
            value = """
                SELECT r FROM Ride r
                JOIN FETCH r.host
                WHERE r.status = :status AND r.departureAt > :now
                ORDER BY r.departureAt ASC
            """,
            countQuery = "SELECT COUNT(r) FROM Ride r WHERE r.status = :status AND r.departureAt > :now"
    )
    Slice<Ride> findUpcoming(
            @Param("status") RideStatus status,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    //내가 방장인 글
    @Query("SELECT r FROM Ride r JOIN FETCH r.host WHERE r.host.id = :hostId ORDER BY r.departureAt DESC")
    List<Ride> findByHostId(@Param("hostId") Long hostId);

    //출발 시간 지난 글 상태 일괄 변경 (스케줄러용)
    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE Ride r SET r.status = :toStatus
        WHERE r.status IN :fromStatuses AND r.departureAt < :before
    """)
    int bulkUpdateStatus(
            @Param("fromStatuses") Collection<RideStatus> fromStatuses,
            @Param("toStatus") RideStatus toStatus,
            @Param("before") LocalDateTime before
    );
}
