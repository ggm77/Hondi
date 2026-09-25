package com.seohamin.hondi.domain.ride.entity.participant;

import com.seohamin.hondi.domain.ride.entity.Ride;
import com.seohamin.hondi.domain.user.entity.User;
import com.seohamin.hondi.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 동승 모집글 참여 정보
 * 행이 있으면 참여 중, 나가면 삭제됨
 * 방장은 참여자에 포함되지 않음
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "ride_participant",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_ride_participant", columnNames = {"ride_id", "user_id"})
        },
        indexes = {
                @Index(name = "idx_ride_participant_user", columnList = "user_id")
        }
)
public class RideParticipant extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ride_id", nullable = false)
    private Ride ride;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    public RideParticipant(
            final Ride ride,
            final User user
    ){
        this.ride = ride;
        this.user = user;
    }
}
