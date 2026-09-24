package com.seohamin.hondi.domain.ride.service.list;

import com.seohamin.hondi.domain.ride.dto.RideMyStatus;
import com.seohamin.hondi.domain.ride.dto.list.RideListItemResponseDto;
import com.seohamin.hondi.domain.ride.dto.list.RideListResponseDto;
import com.seohamin.hondi.domain.ride.entity.Ride;
import com.seohamin.hondi.domain.ride.entity.RideStatus;
import com.seohamin.hondi.domain.ride.entity.participant.ParticipantStatus;
import com.seohamin.hondi.domain.ride.repository.RideRepository;
import com.seohamin.hondi.domain.ride.repository.participant.RideParticipantRepository;
import com.seohamin.hondi.domain.ride.service.ServiceAreaValidator;
import com.seohamin.hondi.domain.ride.service.matching.RideMatchResult;
import com.seohamin.hondi.domain.ride.service.matching.RideMatcher;
import com.seohamin.hondi.domain.user.entity.User;
import com.seohamin.hondi.domain.user.repository.UserRepository;
import com.seohamin.hondi.global.exception.CustomException;
import com.seohamin.hondi.global.exception.constants.ExceptionCode;
import com.seohamin.hondi.global.util.LatLonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RideListService {

    //매칭 검색 결과 최대 개수
    private static final int MAX_MATCH_SIZE = 50;

    private final RideRepository rideRepository;
    private final RideParticipantRepository rideParticipantRepository;
    private final UserRepository userRepository;
    private final RideMatcher rideMatcher;
    private final ServiceAreaValidator serviceAreaValidator;

    /**
     * 출발지, 도착지, 시간이 비슷한 모집글을 점수 높은 순으로 추천하는 메서드
     * 자기 글은 제외
     * @param originLat 출발지 위도
     * @param originLon 출발지 경도
     * @param destLat 도착지 위도
     * @param destLon 도착지 경도
     * @param departureAt 희망 출발 시간 (null이면 현재 시간)
     * @param size 최대 개수 (1~50)
     * @param userId 검색하는 유저 아이디
     * @return 추천 모집글 리스트
     */
    @Transactional(readOnly = true)
    public RideListResponseDto matchRides(
            final BigDecimal originLat,
            final BigDecimal originLon,
            final BigDecimal destLat,
            final BigDecimal destLon,
            final LocalDateTime departureAt,
            final Integer size,
            final Long userId
    ){
        // 1) 파라미터 검사
        serviceAreaValidator.assertInServiceArea(originLat, originLon);
        serviceAreaValidator.assertInServiceArea(destLat, destLon);
        if(size == null || size <= 0 || size > MAX_MATCH_SIZE){
            throw new CustomException(ExceptionCode.INVALID_PAGING_PARAMETER);
        }

        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_EXIST));

        // 2) 시간 범위 계산 (이미 출발한 글 제외)
        final LocalDateTime now = LocalDateTime.now();
        final LocalDateTime targetAt = departureAt != null ? departureAt : now;
        final LocalDateTime fromAt = max(targetAt.minusMinutes(RideMatcher.TIME_WINDOW_MINUTES), now);
        final LocalDateTime toAt = targetAt.plusMinutes(RideMatcher.TIME_WINDOW_MINUTES);

        // 3) 출발지 기준 바운딩 박스 계산
        final BigDecimal latDiff = BigDecimal.valueOf(LatLonUtil.latDiff(RideMatcher.ORIGIN_RADIUS_KM));
        final BigDecimal lonDiff = BigDecimal.valueOf(LatLonUtil.lonDiff(originLat, RideMatcher.ORIGIN_RADIUS_KM));

        // 4) DB에서 1차 필터링
        final List<Ride> candidates = rideRepository.findMatchCandidates(
                RideStatus.RECRUITING,
                fromAt,
                toAt,
                originLat.subtract(latDiff),
                originLat.add(latDiff),
                originLon.subtract(lonDiff),
                originLon.add(lonDiff)
        );

        // 5) 거리, 시간으로 점수 매기기
        final List<RideMatchResult> results = rideMatcher.match(
                candidates, user, originLat, originLon, destLat, destLon, targetAt
        );

        final List<RideListItemResponseDto> items = results.stream()
                .limit(size)
                .map(RideListItemResponseDto::new)
                .toList();

        return new RideListResponseDto(items, results.size() > size);
    }

    /**
     * 출발 예정인 모집 중인 글을 출발 시간 순으로 조회하는 메서드
     * @param page 페이지
     * @param size 페이지 크기 (1~100)
     * @param userId 조회하는 유저 아이디
     * @return 모집글 리스트
     */
    @Transactional(readOnly = true)
    public RideListResponseDto getUpcomingRides(
            final Integer page,
            final Integer size,
            final Long userId
    ){
        if(size == null || size <= 0 || size > 100 || page == null || page < 0){
            throw new CustomException(ExceptionCode.INVALID_PAGING_PARAMETER);
        }

        final Slice<Ride> rides = rideRepository.findUpcoming(
                RideStatus.RECRUITING,
                LocalDateTime.now(),
                PageRequest.of(page, size)
        );

        final List<RideListItemResponseDto> items = rides.stream()
                .map(ride -> new RideListItemResponseDto(
                        ride,
                        ride.isHost(userId) ? RideMyStatus.HOST : RideMyStatus.NONE
                ))
                .toList();

        return new RideListResponseDto(items, rides.hasNext());
    }

    /**
     * 내가 만든 글과 참여(신청 포함) 중인 글을 조회하는 메서드
     * 출발 시간 최신 순으로 정렬
     * @param userId 유저 아이디
     * @return 모집글 리스트
     */
    @Transactional(readOnly = true)
    public RideListResponseDto getMyRides(final Long userId){

        final List<RideListItemResponseDto> items = new ArrayList<>();

        // 1) 방장인 글
        rideRepository.findByHostId(userId)
                .forEach(ride -> items.add(new RideListItemResponseDto(ride, RideMyStatus.HOST)));

        // 2) 신청했거나 참여 중인 글
        rideParticipantRepository.findByUserIdAndStatusIn(
                userId,
                List.of(ParticipantStatus.REQUESTED, ParticipantStatus.ACCEPTED)
        ).forEach(p -> items.add(new RideListItemResponseDto(
                p.getRide(),
                RideMyStatus.valueOf(p.getStatus().name())
        )));

        // 3) 출발 시간 최신 순으로 정렬
        final List<RideListItemResponseDto> sorted = items.stream()
                .sorted(Comparator.comparing(RideListItemResponseDto::getDepartureAt).reversed())
                .toList();

        return new RideListResponseDto(sorted, false);
    }

    private static LocalDateTime max(final LocalDateTime a, final LocalDateTime b){
        return a.isAfter(b) ? a : b;
    }
}
