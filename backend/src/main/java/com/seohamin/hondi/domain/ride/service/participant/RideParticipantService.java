package com.seohamin.hondi.domain.ride.service.participant;

import com.seohamin.hondi.domain.chat.entity.ChatType;
import com.seohamin.hondi.domain.chat.service.ChatService;
import com.seohamin.hondi.domain.ride.dto.participant.RideParticipantDecisionRequestDto;
import com.seohamin.hondi.domain.ride.dto.participant.RideParticipantRequestDto;
import com.seohamin.hondi.domain.ride.dto.participant.RideParticipantResponseDto;
import com.seohamin.hondi.domain.ride.entity.Ride;
import com.seohamin.hondi.domain.ride.entity.RideStatus;
import com.seohamin.hondi.domain.ride.entity.participant.ParticipantStatus;
import com.seohamin.hondi.domain.ride.entity.participant.RideParticipant;
import com.seohamin.hondi.domain.ride.repository.RideRepository;
import com.seohamin.hondi.domain.ride.repository.participant.RideParticipantRepository;
import com.seohamin.hondi.domain.user.entity.User;
import com.seohamin.hondi.domain.user.repository.UserRepository;
import com.seohamin.hondi.global.exception.CustomException;
import com.seohamin.hondi.global.exception.constants.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RideParticipantService {

    private final RideRepository rideRepository;
    private final RideParticipantRepository rideParticipantRepository;
    private final UserRepository userRepository;
    private final ChatService chatService;

    /**
     * 모집글에 참여 신청하는 메서드
     * 나갔던 유저는 다시 신청 가능, 거절된 유저는 불가
     * @param rideId 모집글 아이디
     * @param rideParticipantRequestDto 신청 메세지
     * @param userId 신청하는 유저 아이디
     * @return 참여 신청 정보 DTO
     */
    @Transactional
    public RideParticipantResponseDto requestJoin(
            final Long rideId,
            final RideParticipantRequestDto rideParticipantRequestDto,
            final Long userId
    ){
        // 1) 모집글, 유저 조회
        final Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new CustomException(ExceptionCode.RIDE_NOT_EXIST));
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_EXIST));

        // 2) 자기 글인지 확인
        if(ride.isHost(userId)){
            throw new CustomException(ExceptionCode.CANNOT_JOIN_OWN_RIDE);
        }

        // 3) 모집 중인지 확인
        assertRecruiting(ride);

        final String message = rideParticipantRequestDto.getMessage();

        // 4) 이전 신청 기록 확인
        final Optional<RideParticipant> existing = rideParticipantRepository.findByRideIdAndUserId(rideId, userId);
        if(existing.isPresent()){
            final RideParticipant participant = existing.get();

            switch (participant.getStatus()) {
                case REQUESTED, ACCEPTED -> throw new CustomException(ExceptionCode.RIDE_ALREADY_REQUESTED);
                case REJECTED -> throw new CustomException(ExceptionCode.RIDE_REJECTED);
                case LEFT -> participant.request(message);
            }

            return new RideParticipantResponseDto(participant);
        }

        // 5) 신청 저장
        final RideParticipant saved = rideParticipantRepository.save(RideParticipant.builder()
                .ride(ride)
                .user(user)
                .message(message)
                .build());

        return new RideParticipantResponseDto(saved);
    }

    /**
     * 방장이 참여 신청을 수락/거절하는 메서드
     * 수락시 인원이 늘어나고, 다 차면 FULL로 바뀜
     * 동시에 여러명 수락해도 인원 초과 안되게 모집글에 락을 걸고 처리
     * @param rideId 모집글 아이디
     * @param participantId 참여 신청 아이디
     * @param decisionRequestDto ACCEPTED 또는 REJECTED
     * @param userId 요청한 유저 (방장) 아이디
     * @return 처리된 참여 신청 정보 DTO
     */
    @Transactional
    public RideParticipantResponseDto decide(
            final Long rideId,
            final Long participantId,
            final RideParticipantDecisionRequestDto decisionRequestDto,
            final Long userId
    ){
        // 1) 모집글 락 걸고 조회
        final Ride ride = rideRepository.findByIdForUpdate(rideId)
                .orElseThrow(() -> new CustomException(ExceptionCode.RIDE_NOT_EXIST));

        // 2) 방장인지 확인
        if(!ride.isHost(userId)){
            throw new CustomException(ExceptionCode.NOT_RIDE_HOST);
        }

        // 3) 참여 신청 조회
        final RideParticipant participant = rideParticipantRepository.findById(participantId)
                .filter(p -> p.getRide().getId().equals(rideId))
                .orElseThrow(() -> new CustomException(ExceptionCode.PARTICIPANT_NOT_EXIST));

        // 4) 신청 상태일 때만 처리 가능
        if(participant.getStatus() != ParticipantStatus.REQUESTED){
            throw new CustomException(ExceptionCode.INVALID_PARTICIPANT_STATUS);
        }

        // 5) 수락 or 거절
        switch (decisionRequestDto.getStatus()) {
            case ACCEPTED -> {
                assertRecruiting(ride);
                ride.increaseCount();
                participant.accept();
                chatService.sendSystemChat(ride, ChatType.ENTER, participant.getUser().getNickname() + "님이 참여했어요.");
            }
            case REJECTED -> participant.reject();
            default -> throw new CustomException(ExceptionCode.INVALID_PARTICIPANT_STATUS);
        }

        return new RideParticipantResponseDto(participant);
    }

    /**
     * 참여 신청을 취소하거나 참여 중인 모집글에서 나가는 메서드
     * 참여 중이었다면 인원이 줄어들고, FULL이었다면 다시 RECRUITING으로 바뀜
     * 출발 이후에는 나갈 수 없음
     * @param rideId 모집글 아이디
     * @param userId 나가는 유저 아이디
     */
    @Transactional
    public void leave(
            final Long rideId,
            final Long userId
    ){
        // 1) 모집글 락 걸고 조회
        final Ride ride = rideRepository.findByIdForUpdate(rideId)
                .orElseThrow(() -> new CustomException(ExceptionCode.RIDE_NOT_EXIST));

        // 2) 참여 신청 조회
        final RideParticipant participant = rideParticipantRepository.findByRideIdAndUserId(rideId, userId)
                .orElseThrow(() -> new CustomException(ExceptionCode.PARTICIPANT_NOT_EXIST));

        // 3) 상태에 따라 처리
        switch (participant.getStatus()) {
            case REQUESTED -> participant.leave();
            case ACCEPTED -> {
                if(ride.getStatus() != RideStatus.RECRUITING && ride.getStatus() != RideStatus.FULL){
                    throw new CustomException(ExceptionCode.RIDE_NOT_EDITABLE);
                }
                ride.decreaseCount();
                participant.leave();
                chatService.sendSystemChat(ride, ChatType.EXIT, participant.getUser().getNickname() + "님이 나갔어요.");
            }
            default -> throw new CustomException(ExceptionCode.INVALID_PARTICIPANT_STATUS);
        }
    }

    /**
     * 방장이 참여 신청자와 참여자 목록을 조회하는 메서드
     * @param rideId 모집글 아이디
     * @param userId 요청한 유저 (방장) 아이디
     * @return 신청 중, 참여 중인 유저 리스트
     */
    @Transactional(readOnly = true)
    public List<RideParticipantResponseDto> getParticipants(
            final Long rideId,
            final Long userId
    ){
        final Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new CustomException(ExceptionCode.RIDE_NOT_EXIST));

        if(!ride.isHost(userId)){
            throw new CustomException(ExceptionCode.NOT_RIDE_HOST);
        }

        return rideParticipantRepository.findByRideIdAndStatusIn(
                        rideId,
                        List.of(ParticipantStatus.REQUESTED, ParticipantStatus.ACCEPTED)
                ).stream()
                .map(RideParticipantResponseDto::new)
                .toList();
    }

    //모집 중이고 출발 전인지 확인
    private void assertRecruiting(final Ride ride){
        if(ride.getStatus() == RideStatus.FULL){
            throw new CustomException(ExceptionCode.RIDE_FULL);
        }
        if(ride.getStatus() != RideStatus.RECRUITING || !ride.getDepartureAt().isAfter(LocalDateTime.now())){
            throw new CustomException(ExceptionCode.RIDE_NOT_RECRUITING);
        }
    }
}
