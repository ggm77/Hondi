package com.seohamin.hondi.domain.chat.service;

import com.seohamin.hondi.domain.chat.dto.ChatRequestDto;
import com.seohamin.hondi.domain.chat.dto.ChatResponseDto;
import com.seohamin.hondi.domain.chat.dto.list.ChatListResponseDto;
import com.seohamin.hondi.domain.chat.entity.Chat;
import com.seohamin.hondi.domain.chat.entity.ChatType;
import com.seohamin.hondi.domain.chat.repository.ChatRepository;
import com.seohamin.hondi.domain.chat.service.event.ChatCreatedEvent;
import com.seohamin.hondi.domain.chat.service.room.ChatRoomService;
import com.seohamin.hondi.domain.ride.entity.Ride;
import com.seohamin.hondi.domain.ride.repository.RideRepository;
import com.seohamin.hondi.domain.user.entity.User;
import com.seohamin.hondi.domain.user.repository.UserRepository;
import com.seohamin.hondi.global.exception.CustomException;
import com.seohamin.hondi.global.exception.constants.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final RideRepository rideRepository;
    private final UserRepository userRepository;
    private final ChatRoomService chatRoomService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 채팅을 저장하고 채팅방에 전송하는 메서드
     * 채팅방 멤버(방장, 참여 중인 유저)만 전송 가능
     * @param rideId 채팅방 (모집글) 아이디
     * @param chatRequestDto 채팅 내용
     * @param userId 보내는 유저 아이디
     * @return 저장된 채팅 DTO
     */
    @Transactional
    public ChatResponseDto sendChat(
            final Long rideId,
            final ChatRequestDto chatRequestDto,
            final Long userId
    ){
        // 1) 채팅방 멤버인지 확인
        final Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new CustomException(ExceptionCode.CHAT_ROOM_NOT_EXIST));
        if(!chatRoomService.isMember(rideId, userId)){
            throw new CustomException(ExceptionCode.CHAT_ROOM_NOT_JOINED);
        }

        // 2) 보내는 유저 조회
        final User sender = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_EXIST));

        // 3) 저장 후 전송
        return saveAndPublish(ride, sender, ChatType.MESSAGE, chatRequestDto.getMessage());
    }

    /**
     * 입장, 퇴장, 취소 같은 시스템 메세지를 보내는 메서드
     * 다른 서비스의 트랜잭션 안에서 호출됨
     * @param ride 채팅방 (모집글)
     * @param type 메세지 타입
     * @param message 메세지 내용
     */
    @Transactional
    public void sendSystemChat(
            final Ride ride,
            final ChatType type,
            final String message
    ){
        saveAndPublish(ride, null, type, message);
    }

    /**
     * lastChatId 이후의 채팅을 가져오는 메서드
     * 채팅방 처음 들어올 때는 lastChatId=0으로 요청하고, 이후 nextCursor로 이어서 요청
     * @param rideId 채팅방 (모집글) 아이디
     * @param lastChatId 마지막으로 받은 채팅 아이디
     * @param size 가져올 개수 (1~100)
     * @param userId 요청한 유저 아이디
     * @return 채팅 리스트
     */
    @Transactional(readOnly = true)
    public ChatListResponseDto syncChat(
            final Long rideId,
            final Long lastChatId,
            final Integer size,
            final Long userId
    ){
        // 1) 파라미터 검사
        if(size == null || size <= 0 || size > 100 || lastChatId == null || lastChatId < 0){
            throw new CustomException(ExceptionCode.INVALID_PAGING_PARAMETER);
        }

        // 2) 채팅방 멤버인지 확인
        if(!chatRoomService.isMember(rideId, userId)){
            throw new CustomException(ExceptionCode.CHAT_ROOM_NOT_JOINED);
        }

        // 3) 하나 더 가져와서 다음 페이지 있는지 확인
        final List<Chat> chats = chatRepository.findAfter(rideId, lastChatId, PageRequest.of(0, size + 1));
        final boolean hasMore = chats.size() > size;

        final List<ChatResponseDto> items = chats.stream()
                .limit(size)
                .map(ChatResponseDto::new)
                .toList();

        final Long nextCursor = items.isEmpty() ? lastChatId : items.getLast().getId();

        return new ChatListResponseDto(items, nextCursor, hasMore);
    }

    //채팅 저장하고 커밋 후 전송되도록 이벤트 발행
    private ChatResponseDto saveAndPublish(
            final Ride ride,
            final User sender,
            final ChatType type,
            final String message
    ){
        final Chat chat = chatRepository.save(Chat.builder()
                .ride(ride)
                .sender(sender)
                .type(type)
                .message(message)
                .build());

        final ChatResponseDto chatResponseDto = new ChatResponseDto(chat);
        eventPublisher.publishEvent(new ChatCreatedEvent(chatResponseDto));

        return chatResponseDto;
    }
}
