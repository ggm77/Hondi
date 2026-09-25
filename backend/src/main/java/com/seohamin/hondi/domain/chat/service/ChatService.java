package com.seohamin.hondi.domain.chat.service;

import com.seohamin.hondi.domain.chat.dto.ChatMessageListResponseDto;
import com.seohamin.hondi.domain.chat.dto.ChatMessageRequestDto;
import com.seohamin.hondi.domain.chat.dto.ChatMessageResponseDto;
import com.seohamin.hondi.domain.chat.dto.ChatRoomSummaryListResponseDto;
import com.seohamin.hondi.domain.chat.dto.ChatRoomSummaryResponseDto;
import com.seohamin.hondi.domain.chat.entity.ChatMessage;
import com.seohamin.hondi.domain.chat.entity.ChatReadStatus;
import com.seohamin.hondi.domain.chat.repository.ChatMessageRepository;
import com.seohamin.hondi.domain.chat.repository.ChatReadStatusRepository;
import com.seohamin.hondi.domain.ride.entity.Ride;
import com.seohamin.hondi.domain.ride.repository.RideRepository;
import com.seohamin.hondi.domain.ride.repository.participant.RideParticipantRepository;
import com.seohamin.hondi.domain.user.entity.User;
import com.seohamin.hondi.domain.user.repository.UserRepository;
import com.seohamin.hondi.global.exception.CustomException;
import com.seohamin.hondi.global.exception.constants.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private static final int MAX_PAGE_SIZE = 100;

    private final ChatMessageRepository chatMessageRepository;
    private final ChatReadStatusRepository chatReadStatusRepository;
    private final RideRepository rideRepository;
    private final RideParticipantRepository rideParticipantRepository;
    private final UserRepository userRepository;

    /**
     * 채팅 메시지를 보내는 메서드
     * 방장이거나 참여 중이어야 보낼 수 있음
     * @param rideId 모집글(채팅방) 아이디
     * @param requestDto 메시지 내용
     * @param userId 보내는 유저 아이디
     * @return 저장된 메시지 DTO
     */
    @Transactional
    public ChatMessageResponseDto sendMessage(
            final Long rideId,
            final ChatMessageRequestDto requestDto,
            final Long userId
    ){
        //같은 방의 앞선 메시지가 커밋된 후 ID를 발급받아 폴링 누락 방지
        final Ride ride = getRideForUpdateAndAssertMember(rideId, userId);

        final User sender = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_EXIST));

        final ChatMessage message = chatMessageRepository.save(ChatMessage.builder()
                .ride(ride)
                .sender(sender)
                .content(requestDto.getContent())
                .build());

        return new ChatMessageResponseDto(message);
    }

    /**
     * 채팅 메시지를 조회하는 메서드
     * afterId가 있으면 폴링용(그 이후 온 메시지), beforeId가 있으면 과거 메시지 스크롤용
     * 둘 다 없으면 가장 최근 메시지들을 줌
     * @param rideId 모집글(채팅방) 아이디
     * @param afterId 이 메시지 이후 것만 (폴링용)
     * @param beforeId 이 메시지 이전 것만 (과거 조회용)
     * @param size 최대 개수 (1~100)
     * @param userId 조회하는 유저 아이디
     * @return 시간 오름차순으로 정렬된 메시지 목록
     */
    @Transactional(readOnly = true)
    public ChatMessageListResponseDto getMessages(
            final Long rideId,
            final Long afterId,
            final Long beforeId,
            final Integer size,
            final Long userId
    ){
        if(size == null || size <= 0 || size > MAX_PAGE_SIZE){
            throw new CustomException(ExceptionCode.INVALID_PAGING_PARAMETER);
        }

        getRideAndAssertMember(rideId, userId);

        final Pageable pageable = PageRequest.of(0, size + 1);

        if(afterId != null){
            final List<ChatMessage> messages = chatMessageRepository.findAfter(rideId, afterId, pageable);
            return toListDto(messages, size, false);
        }

        final List<ChatMessage> messages = beforeId != null
                ? chatMessageRepository.findBefore(rideId, beforeId, pageable)
                : chatMessageRepository.findLatest(rideId, pageable);

        return toListDto(messages, size, true);
    }

    /**
     * 읽은 위치를 갱신하는 메서드
     * @param rideId 모집글(채팅방) 아이디
     * @param lastMessageId 마지막으로 읽은 메시지 아이디
     * @param userId 요청한 유저 아이디
     */
    @Transactional
    public void markRead(
            final Long rideId,
            final Long lastMessageId,
            final Long userId
    ){
        //최초 읽음 기록 생성과 읽은 위치 갱신을 함께 직렬화
        getRideForUpdateAndAssertMember(rideId, userId);

        if(!chatMessageRepository.existsByIdAndRideId(lastMessageId, rideId)){
            throw new CustomException(ExceptionCode.CHAT_MESSAGE_NOT_EXIST);
        }

        chatReadStatusRepository.findByRideIdAndUserId(rideId, userId)
                .ifPresentOrElse(
                        status -> status.updateLastReadMessageId(lastMessageId),
                        () -> chatReadStatusRepository.save(ChatReadStatus.builder()
                                .ride(rideRepository.getReferenceById(rideId))
                                .user(userRepository.getReferenceById(userId))
                                .lastReadMessageId(lastMessageId)
                                .build())
                );
    }

    /**
     * 내가 방장이거나 참여 중인 채팅방 목록을 마지막 메시지, 안 읽은 개수와 함께 조회하는 메서드
     * 최근 메시지가 온 순서로 정렬 (메시지가 없으면 뒤로)
     * @param userId 유저 아이디
     * @return 채팅방 목록
     */
    @Transactional(readOnly = true)
    public ChatRoomSummaryListResponseDto getMyChatRooms(final Long userId){

        final List<Ride> rides = new ArrayList<>();
        rides.addAll(rideRepository.findByHostId(userId));
        rideParticipantRepository.findByUserIdWithRide(userId)
                .forEach(p -> rides.add(p.getRide()));

        final List<ChatRoomSummaryResponseDto> rooms = rides.stream()
                .map(ride -> toSummaryDto(ride, userId))
                .sorted(Comparator.comparing(
                        ChatRoomSummaryResponseDto::getLastMessageAt,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .toList();

        return new ChatRoomSummaryListResponseDto(rooms);
    }

    //모집글을 조회하고 방장/참여자인지 확인
    private Ride getRideAndAssertMember(final Long rideId, final Long userId){
        final Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new CustomException(ExceptionCode.RIDE_NOT_EXIST));

        assertMember(ride, userId);

        return ride;
    }

    private Ride getRideForUpdateAndAssertMember(final Long rideId, final Long userId){
        final Ride ride = rideRepository.findByIdForUpdate(rideId)
                .orElseThrow(() -> new CustomException(ExceptionCode.RIDE_NOT_EXIST));

        assertMember(ride, userId);

        return ride;
    }

    private void assertMember(final Ride ride, final Long userId){
        if(!ride.isHost(userId) && !rideParticipantRepository.existsByRideIdAndUserId(ride.getId(), userId)){
            throw new CustomException(ExceptionCode.NOT_RIDE_MEMBER);
        }
    }

    //채팅방 목록 아이템 DTO 만들기
    private ChatRoomSummaryResponseDto toSummaryDto(final Ride ride, final Long userId){
        final ChatMessage lastMessage = chatMessageRepository
                .findTopByRideIdOrderByIdDesc(ride.getId())
                .orElse(null);

        final long lastReadId = chatReadStatusRepository
                .findByRideIdAndUserId(ride.getId(), userId)
                .map(ChatReadStatus::getLastReadMessageId)
                .orElse(0L);

        final long unreadCount = chatMessageRepository.countByRideIdAndIdGreaterThan(ride.getId(), lastReadId);

        return new ChatRoomSummaryResponseDto(ride, lastMessage, unreadCount);
    }

    //size+1개 조회해서 hasNext 판단하고, 최신순으로 가져온 경우 시간 오름차순으로 뒤집기
    private ChatMessageListResponseDto toListDto(
            final List<ChatMessage> messages,
            final int size,
            final boolean fetchedDesc
    ){
        final boolean hasNext = messages.size() > size;
        final List<ChatMessage> trimmed = hasNext ? messages.subList(0, size) : messages;

        if(fetchedDesc){
            Collections.reverse(trimmed);
        }

        final List<ChatMessageResponseDto> items = trimmed.stream()
                .map(ChatMessageResponseDto::new)
                .toList();

        return new ChatMessageListResponseDto(items, hasNext);
    }
}
