package com.seohamin.hondi.domain.ride.service;

import com.seohamin.hondi.domain.chat.entity.ChatType;
import com.seohamin.hondi.domain.chat.service.ChatService;
import com.seohamin.hondi.domain.ride.dto.RideMyStatus;
import com.seohamin.hondi.domain.ride.dto.RideRequestDto;
import com.seohamin.hondi.domain.ride.dto.RideResponseDto;
import com.seohamin.hondi.domain.ride.entity.Ride;
import com.seohamin.hondi.domain.ride.entity.RideStatus;
import com.seohamin.hondi.domain.ride.entity.participant.ParticipantStatus;
import com.seohamin.hondi.domain.ride.entity.participant.RideParticipant;
import com.seohamin.hondi.domain.ride.repository.RideRepository;
import com.seohamin.hondi.domain.ride.repository.participant.RideParticipantRepository;
import com.seohamin.hondi.domain.user.dto.UserSimpleResponseDto;
import com.seohamin.hondi.domain.user.entity.User;
import com.seohamin.hondi.domain.user.repository.UserRepository;
import com.seohamin.hondi.global.exception.CustomException;
import com.seohamin.hondi.global.exception.constants.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RideService {

    private final RideRepository rideRepository;
    private final RideParticipantRepository rideParticipantRepository;
    private final UserRepository userRepository;
    private final ServiceAreaValidator serviceAreaValidator;
    private final ChatService chatService;

    /**
     * 동승 모집글을 생성하는 메서드
     * @param rideRequestDto 모집글 정보
     * @param userId 작성자(방장) 아이디
     * @return 생성된 모집글 DTO
     */
    @Transactional
    public RideResponseDto createRide(
            final RideRequestDto rideRequestDto,
            final Long userId
    ){
        // 1) 출발지, 도착지가 서비스 지역인지 확인
        serviceAreaValidator.assertInServiceArea(rideRequestDto.getOriginLat(), rideRequestDto.getOriginLon());
        serviceAreaValidator.assertInServiceArea(rideRequestDto.getDestLat(), rideRequestDto.getDestLon());

        // 2) 작성자 조회
        final User host = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_EXIST));

        // 3) 모집글 저장
        final Ride ride = rideRepository.save(Ride.builder()
                .host(host)
                .originName(rideRequestDto.getOriginName())
                .originLat(rideRequestDto.getOriginLat())
                .originLon(rideRequestDto.getOriginLon())
                .destName(rideRequestDto.getDestName())
                .destLat(rideRequestDto.getDestLat())
                .destLon(rideRequestDto.getDestLon())
                .departureAt(rideRequestDto.getDepartureAt())
                .capacity(rideRequestDto.getCapacity())
                .memo(rideRequestDto.getMemo())
                .build());

        return new RideResponseDto(ride, RideMyStatus.HOST, List.of());
    }

    /**
     * 모집글을 조회하는 메서드
     * 수락된 참여자 목록과 조회한 유저의 참여 상태를 같이 보여줌
     * @param rideId 모집글 아이디
     * @param userId 조회하는 유저 아이디
     * @return 모집글 DTO
     */
    @Transactional(readOnly = true)
    public RideResponseDto getRide(
            final Long rideId,
            final Long userId
    ){
        final Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new CustomException(ExceptionCode.RIDE_NOT_EXIST));

        return toResponseDto(ride, userId);
    }

    /**
     * 모집글을 수정하는 메서드
     * 방장만 수정 가능하고, 모집 중이거나 인원이 다 찬 상태에서만 가능
     * @param rideId 모집글 아이디
     * @param rideRequestDto 수정할 정보 (출발 시간, 최대 인원, 메모)
     * @param userId 요청한 유저 아이디
     * @return 수정된 모집글 DTO
     */
    @Transactional
    public RideResponseDto updateRide(
            final Long rideId,
            final RideRequestDto rideRequestDto,
            final Long userId
    ){
        // 1) 인원 변경이 있을 수 있어서 락 걸고 조회
        final Ride ride = rideRepository.findByIdForUpdate(rideId)
                .orElseThrow(() -> new CustomException(ExceptionCode.RIDE_NOT_EXIST));

        // 2) 방장인지, 수정 가능한 상태인지 확인
        assertHost(ride, userId);
        assertEditable(ride);

        // 3) 변경할 값만 변경
        if(rideRequestDto.getDepartureAt() != null){
            ride.updateDepartureAt(rideRequestDto.getDepartureAt());
        }

        if(rideRequestDto.getCapacity() != null){
            if(rideRequestDto.getCapacity() < ride.getCurrentCount()){
                throw new CustomException(ExceptionCode.INVALID_CAPACITY);
            }
            ride.updateCapacity(rideRequestDto.getCapacity());
        }

        if(rideRequestDto.getMemo() != null){
            ride.updateMemo(rideRequestDto.getMemo());
        }

        return toResponseDto(ride, userId);
    }

    /**
     * 모집글을 취소하는 메서드
     * 삭제하지 않고 CANCELED 상태로 변경 (채팅, 신청 기록 유지)
     * @param rideId 모집글 아이디
     * @param userId 요청한 유저 아이디
     */
    @Transactional
    public void cancelRide(
            final Long rideId,
            final Long userId
    ){
        final Ride ride = rideRepository.findByIdForUpdate(rideId)
                .orElseThrow(() -> new CustomException(ExceptionCode.RIDE_NOT_EXIST));

        assertHost(ride, userId);
        assertEditable(ride);

        ride.cancel();
        chatService.sendSystemChat(ride, ChatType.SYSTEM, "방장이 모집을 취소했어요.");
    }

    /**
     * 모집글과 유저의 관계를 구하는 메서드
     * @param ride 모집글
     * @param userId 유저 아이디
     * @return 방장, 참여 상태, 관계 없음 중 하나
     */
    public RideMyStatus getMyStatus(final Ride ride, final Long userId){
        if(ride.isHost(userId)){
            return RideMyStatus.HOST;
        }

        final Optional<RideParticipant> participant = rideParticipantRepository.findByRideIdAndUserId(ride.getId(), userId);

        return participant
                .map(p -> RideMyStatus.valueOf(p.getStatus().name()))
                .orElse(RideMyStatus.NONE);
    }

    //방장인지 확인
    private void assertHost(final Ride ride, final Long userId){
        if(!ride.isHost(userId)){
            throw new CustomException(ExceptionCode.NOT_RIDE_HOST);
        }
    }

    //수정, 취소 가능한 상태인지 확인
    private void assertEditable(final Ride ride){
        if(ride.getStatus() != RideStatus.RECRUITING && ride.getStatus() != RideStatus.FULL){
            throw new CustomException(ExceptionCode.RIDE_NOT_EDITABLE);
        }
    }

    //응답 DTO 만들기
    private RideResponseDto toResponseDto(final Ride ride, final Long userId){
        final List<UserSimpleResponseDto> members = rideParticipantRepository
                .findByRideIdAndStatusIn(ride.getId(), List.of(ParticipantStatus.ACCEPTED))
                .stream()
                .map(p -> new UserSimpleResponseDto(p.getUser()))
                .toList();

        return new RideResponseDto(ride, getMyStatus(ride, userId), members);
    }
}
