package com.seohamin.hondi.domain.ride.service.participant;

import com.seohamin.hondi.domain.ride.dto.participant.RideParticipantResponseDto;
import com.seohamin.hondi.domain.ride.entity.Ride;
import com.seohamin.hondi.domain.ride.entity.RideStatus;
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

@Service
@RequiredArgsConstructor
public class RideParticipantService {

    private final RideRepository rideRepository;
    private final RideParticipantRepository rideParticipantRepository;
    private final UserRepository userRepository;

    /**
     * 모집글에 참여하는 메서드
     * 방장 수락 없이 최대 인원 내라면 바로 참여됨, 인원이 다 차면 FULL로 바뀜
     * 동시에 여러명 참여해도 인원 초과 안되게 모집글에 락을 걸고 처리
     * 나갔던 유저도 다시 참여 가능
     * @param rideId 모집글 아이디
     * @param userId 참여하는 유저 아이디
     * @return 참여 정보 DTO
     */
    @Transactional
    public RideParticipantResponseDto join(
            final Long rideId,
            final Long userId
    ){
        // 1) 모집글 락 걸고 조회
        final Ride ride = rideRepository.findByIdForUpdate(rideId)
                .orElseThrow(() -> new CustomException(ExceptionCode.RIDE_NOT_EXIST));

        // 2) 자기 글인지 확인
        if(ride.isHost(userId)){
            throw new CustomException(ExceptionCode.CANNOT_JOIN_OWN_RIDE);
        }

        // 3) 이미 참여 중인지 확인
        if(rideParticipantRepository.existsByRideIdAndUserId(rideId, userId)){
            throw new CustomException(ExceptionCode.RIDE_ALREADY_JOINED);
        }

        // 4) 모집 중이고 자리가 남아있는지 확인
        assertRecruiting(ride);

        // 5) 참여 저장
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_EXIST));
        final RideParticipant participant = rideParticipantRepository.save(RideParticipant.builder()
                .ride(ride)
                .user(user)
                .build());

        // 6) 인원 증가
        ride.increaseCount();

        return new RideParticipantResponseDto(participant);
    }

    /**
     * 참여 중인 모집글에서 나가는 메서드
     * 인원이 줄어들고, FULL이었다면 다시 RECRUITING으로 바뀜
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

        // 2) 참여 정보 조회
        final RideParticipant participant = rideParticipantRepository.findByRideIdAndUserId(rideId, userId)
                .orElseThrow(() -> new CustomException(ExceptionCode.PARTICIPANT_NOT_EXIST));

        // 3) 출발 전인지 확인
        if(ride.getStatus() != RideStatus.RECRUITING && ride.getStatus() != RideStatus.FULL){
            throw new CustomException(ExceptionCode.RIDE_NOT_EDITABLE);
        }

        // 4) 인원 감소 후 참여 정보 삭제
        ride.decreaseCount();
        rideParticipantRepository.delete(participant);
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
