package com.seohamin.hondi.domain.ride.entity;

import com.seohamin.hondi.domain.ride.entity.participant.RideParticipant;
import com.seohamin.hondi.domain.user.entity.User;
import com.seohamin.hondi.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 동승 모집글 엔티티
 * 인원수(currentCount)는 방장을 포함한 수
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "ride",
        indexes = {
                @Index(name = "idx_ride_status_departure", columnList = "status, departure_at"),
                @Index(name = "idx_ride_origin_lat_lon", columnList = "origin_lat, origin_lon"),
                @Index(name = "idx_ride_host_id", columnList = "host_id")
        }
)
public class Ride extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //모집글 작성자 (방장)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    //출발지 이름 (ex. 제주국제공항)
    @Column(length = 100, nullable = false)
    private String originName;

    @Column(precision = 9, scale = 6, nullable = false)
    private BigDecimal originLat;

    @Column(precision = 9, scale = 6, nullable = false)
    private BigDecimal originLon;

    //도착지 이름 (ex. 성산일출봉)
    @Column(length = 100, nullable = false)
    private String destName;

    @Column(precision = 9, scale = 6, nullable = false)
    private BigDecimal destLat;

    @Column(precision = 9, scale = 6, nullable = false)
    private BigDecimal destLon;

    //출발 희망 시간
    @Column(nullable = false)
    private LocalDateTime departureAt;

    //방장 포함 최대 인원
    @Column(nullable = false)
    private Integer capacity;

    //방장 포함 현재 인원
    @Column(nullable = false)
    private Integer currentCount;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private RideStatus status;

    //하고 싶은 말 (합류 지점, 짐 여부 등)
    @Column(length = 500, nullable = true)
    private String memo;

    @OneToMany(mappedBy = "ride", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RideParticipant> participants = new ArrayList<>();

    @Builder
    public Ride(
            final User host,
            final String originName,
            final BigDecimal originLat,
            final BigDecimal originLon,
            final String destName,
            final BigDecimal destLat,
            final BigDecimal destLon,
            final LocalDateTime departureAt,
            final Integer capacity,
            final String memo
    ){
        this.host = host;
        this.originName = originName;
        this.originLat = originLat;
        this.originLon = originLon;
        this.destName = destName;
        this.destLat = destLat;
        this.destLon = destLon;
        this.departureAt = departureAt;
        this.capacity = capacity;
        this.currentCount = 1;
        this.status = RideStatus.RECRUITING;
        this.memo = memo;
    }

    //방장인지 확인
    public boolean isHost(final Long userId){
        return this.host.getId().equals(userId);
    }

    //출발 시간 변경
    public void updateDepartureAt(final LocalDateTime departureAt){
        this.departureAt = departureAt;
    }

    //메모 변경
    public void updateMemo(final String memo){
        this.memo = memo;
    }

    //최대 인원 변경 (검증은 서비스에서 함)
    public void updateCapacity(final int capacity){
        this.capacity = capacity;
    }

    //모집글 취소
    public void cancel(){
        this.status = RideStatus.CANCELED;
    }

    //화면에 보여줄 상태 (FULL은 DB에 저장 안 하고 인원수로 그때그때 계산)
    public RideStatus getDisplayStatus(){
        if(this.status == RideStatus.CANCELED){
            return RideStatus.CANCELED;
        }
        return this.currentCount >= this.capacity ? RideStatus.FULL : RideStatus.RECRUITING;
    }
}
