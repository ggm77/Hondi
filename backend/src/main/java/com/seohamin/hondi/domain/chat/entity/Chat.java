package com.seohamin.hondi.domain.chat.entity;

import com.seohamin.hondi.domain.ride.entity.Ride;
import com.seohamin.hondi.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 채팅 메세지 엔티티
 * 채팅방은 모집글 하나당 하나 (방장 + 수락된 참여자)
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "chat",
        indexes = {
                @Index(name = "idx_chat_ride_id", columnList = "ride_id, id")
        }
)
public class Chat {

    //채팅의 고유 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //채팅방 (모집글)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ride_id", nullable = false, updatable = false)
    private Ride ride;

    //보낸 유저 (시스템 메세지면 null)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = true, updatable = false)
    private User sender;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false, updatable = false)
    private ChatType type;

    @Column(length = 1024, nullable = false, updatable = false)
    private String message;

    //DB 저장 시점
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Chat(
            final Ride ride,
            final User sender,
            final ChatType type,
            final String message
    ){
        this.ride = ride;
        this.sender = sender;
        this.type = type;
        this.message = message;
    }
}
