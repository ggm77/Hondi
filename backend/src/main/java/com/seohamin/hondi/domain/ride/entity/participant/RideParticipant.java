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
                @Index(name = "idx_ride_participant_user", columnList = "user_id, status")
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

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private ParticipantStatus status;

    @Builder
    public RideParticipant(
            final Ride ride,
            final User user
    ){
        this.ride = ride;
        this.user = user;
        this.status = ParticipantStatus.JOINED;
    }

    //다시 참여 (나갔던 유저가 재참여)
    public void rejoin(){
        this.status = ParticipantStatus.JOINED;
    }

    public void leave(){
        this.status = ParticipantStatus.LEFT;
    }
}
