package com.seohamin.hondi.domain.chat.entity;

import com.seohamin.hondi.domain.ride.entity.Ride;
import com.seohamin.hondi.domain.user.entity.User;
import com.seohamin.hondi.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 모집글(Ride) 채팅방의 메시지
 * 채팅방은 모집글과 1:1이라 별도 방 엔티티 없이 ride로 구분함
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "chat_message",
        indexes = {
                @Index(name = "idx_chat_message_ride_id", columnList = "ride_id, id")
        }
)
public class ChatMessage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ride_id", nullable = false)
    private Ride ride;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(length = 1000, nullable = false)
    private String content;

    @Builder
    public ChatMessage(
            final Ride ride,
            final User sender,
            final String content
    ){
        this.ride = ride;
        this.sender = sender;
        this.content = content;
    }
}
