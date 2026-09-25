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
 * 유저가 채팅방(모집글)에서 마지막으로 읽은 메시지 id
 * 채팅방 목록의 안 읽은 메시지 수 계산에 사용
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "chat_read_status",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_chat_read_status", columnNames = {"ride_id", "user_id"})
        }
)
public class ChatReadStatus extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ride_id", nullable = false)
    private Ride ride;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "last_read_message_id", nullable = false)
    private Long lastReadMessageId;

    @Builder
    public ChatReadStatus(
            final Ride ride,
            final User user,
            final Long lastReadMessageId
    ){
        this.ride = ride;
        this.user = user;
        this.lastReadMessageId = lastReadMessageId;
    }

    //읽은 위치 갱신 (이미 읽은 것보다 뒤로 가지 않게 더 큰 값일 때만 갱신)
    public void updateLastReadMessageId(final Long lastReadMessageId){
        if(lastReadMessageId > this.lastReadMessageId){
            this.lastReadMessageId = lastReadMessageId;
        }
    }
}
