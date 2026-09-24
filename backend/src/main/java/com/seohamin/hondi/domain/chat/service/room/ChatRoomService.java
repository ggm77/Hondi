package com.seohamin.hondi.domain.chat.service.room;

import com.seohamin.hondi.domain.chat.dto.ChatResponseDto;
import com.seohamin.hondi.domain.chat.dto.room.ChatRoomResponseDto;
import com.seohamin.hondi.domain.chat.repository.ChatRepository;
import com.seohamin.hondi.domain.ride.entity.Ride;
import com.seohamin.hondi.domain.ride.entity.participant.ParticipantStatus;
import com.seohamin.hondi.domain.ride.entity.participant.RideParticipant;
import com.seohamin.hondi.domain.ride.repository.RideRepository;
import com.seohamin.hondi.domain.ride.repository.participant.RideParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final RideRepository rideRepository;
    private final RideParticipantRepository rideParticipantRepository;
    private final ChatRepository chatRepository;

    /**
     * 유저가 채팅방(모집글)의 멤버인지 확인하는 메서드
     * 방장이거나 참여 중인 유저만 멤버
     * @param rideId 모집글 아이디
     * @param userId 유저 아이디
     * @return 멤버 여부
     */
    @Transactional(readOnly = true)
    public boolean isMember(final Long rideId, final Long userId) {
        return rideRepository.existsByIdAndHostId(rideId, userId)
                || rideParticipantRepository.existsByRideIdAndUserIdAndStatus(rideId, userId, ParticipantStatus.JOINED);
    }

    /**
     * 자신이 참가 중인 채팅방 리스트를 조회하는 메서드
     * 마지막 채팅이 최근인 순으로 정렬 (채팅 없으면 모집글 생성 시간)
     * @param userId 유저 아이디
     * @return 채팅방 리스트
     */
    @Transactional(readOnly = true)
    public List<ChatRoomResponseDto> getChatRooms(final Long userId) {

        // 1) 방장인 모집글과 참여 중인 모집글
        final Stream<Ride> hostRides = rideRepository.findByHostId(userId).stream();
        final Stream<Ride> joinedRides = rideParticipantRepository
                .findByUserIdAndStatusIn(userId, List.of(ParticipantStatus.JOINED))
                .stream()
                .map(RideParticipant::getRide);

        // 2) 마지막 채팅 붙여서 정렬
        return Stream.concat(hostRides, joinedRides)
                .map(ride -> new ChatRoomResponseDto(
                        ride,
                        chatRepository.findTopByRideIdOrderByIdDesc(ride.getId())
                                .map(ChatResponseDto::new)
                                .orElse(null)
                ))
                .sorted(Comparator.comparing(ChatRoomService::lastActivityAt).reversed())
                .toList();
    }

    private static LocalDateTime lastActivityAt(final ChatRoomResponseDto room) {
        return room.getLastChat() != null ? room.getLastChat().getCreatedAt() : room.getDepartureAt();
    }
}
