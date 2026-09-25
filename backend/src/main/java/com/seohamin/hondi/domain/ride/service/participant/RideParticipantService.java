package com.seohamin.hondi.domain.ride.service.participant;

import com.seohamin.hondi.domain.ride.dto.participant.RideParticipantResponseDto;
import com.seohamin.hondi.domain.ride.entity.Ride;
import com.seohamin.hondi.domain.ride.entity.participant.RideParticipant;
import com.seohamin.hondi.domain.ride.repository.RideRepository;
import com.seohamin.hondi.domain.ride.repository.participant.RideParticipantRepository;
import com.seohamin.hondi.domain.user.entity.User;
import com.seohamin.hondi.domain.user.repository.UserRepository;
import com.seohamin.hondi.global.exception.CustomException;
import com.seohamin.hondi.global.exception.constants.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RideParticipantService {

    private final RideRepository rideRepository;
    private final RideParticipantRepository rideParticipantRepository;
    private final UserRepository userRepository;

    @Transactional
    public RideParticipantResponseDto join(
            final Long rideId,
            final Long userId
    ){
        final Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new CustomException(ExceptionCode.RIDE_NOT_EXIST));

        if(ride.isHost(userId)){
            throw new CustomException(ExceptionCode.CANNOT_JOIN_OWN_RIDE);
        }

        if(rideParticipantRepository.existsByRideIdAndUserId(rideId, userId)){
            throw new CustomException(ExceptionCode.RIDE_ALREADY_JOINED);
        }

        assertRecruiting(ride);

        //정원 안에서만 인원을 늘리는 원자적 UPDATE, 락 없이 DB 조건으로 정원을 보장함
        if(rideRepository.increaseCountIfAvailable(rideId) == 0){
            throw new CustomException(ExceptionCode.RIDE_FULL);
        }

        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_EXIST));

        try {
            final RideParticipant participant = rideParticipantRepository.save(RideParticipant.builder()
                    .ride(ride)
                    .user(user)
                    .build());

            return new RideParticipantResponseDto(participant);
        } catch (final DataIntegrityViolationException ex) {
            //거의 동시에 중복 참여 요청이 들어온 경우 (유니크 제약으로 DB가 막아줌)
            throw new CustomException(ExceptionCode.RIDE_ALREADY_JOINED, ex);
        }
    }

    @Transactional
    public void leave(
            final Long rideId,
            final Long userId
    ){
        if(!rideRepository.existsById(rideId)){
            throw new CustomException(ExceptionCode.RIDE_NOT_EXIST);
        }

        final RideParticipant participant = rideParticipantRepository.findByRideIdAndUserId(rideId, userId)
                .orElseThrow(() -> new CustomException(ExceptionCode.PARTICIPANT_NOT_EXIST));

        rideParticipantRepository.delete(participant);
        rideRepository.decreaseCount(rideId);
    }

    private void assertRecruiting(final Ride ride){
        if(!ride.getDepartureAt().isAfter(Instant.now())){
            throw new CustomException(ExceptionCode.RIDE_NOT_RECRUITING);
        }
        if(ride.getCurrentCount() >= ride.getCapacity()){
            throw new CustomException(ExceptionCode.RIDE_FULL);
        }
    }
}
