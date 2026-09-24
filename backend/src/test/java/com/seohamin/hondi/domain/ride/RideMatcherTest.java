package com.seohamin.hondi.domain.ride;

import com.seohamin.hondi.domain.ride.entity.Ride;
import com.seohamin.hondi.domain.ride.service.matching.RideMatchResult;
import com.seohamin.hondi.domain.ride.service.matching.RideMatcher;
import com.seohamin.hondi.domain.user.dto.oauth.UserOauth2AccountsRequestDto;
import com.seohamin.hondi.domain.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RideMatcherTest {

    //제주공항, 공항 근처, 성산일출봉, 애월
    private static final BigDecimal AIRPORT_LAT = new BigDecimal("33.507000");
    private static final BigDecimal AIRPORT_LON = new BigDecimal("126.493000");
    private static final BigDecimal NEAR_AIRPORT_LAT = new BigDecimal("33.510000");
    private static final BigDecimal NEAR_AIRPORT_LON = new BigDecimal("126.499000");
    private static final BigDecimal SEONGSAN_LAT = new BigDecimal("33.458100");
    private static final BigDecimal SEONGSAN_LON = new BigDecimal("126.942500");
    private static final BigDecimal AEWOL_LAT = new BigDecimal("33.463600");
    private static final BigDecimal AEWOL_LON = new BigDecimal("126.331000");

    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2030, 1, 1, 14, 0);

    private final RideMatcher rideMatcher = new RideMatcher();

    private static User user(final long id) {
        final User user = new User(UserOauth2AccountsRequestDto.builder().build());
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private static Ride ride(
            final User host,
            final BigDecimal originLat, final BigDecimal originLon,
            final BigDecimal destLat, final BigDecimal destLon,
            final LocalDateTime departureAt
    ) {
        return Ride.builder()
                .host(host)
                .originName("출발").originLat(originLat).originLon(originLon)
                .destName("도착").destLat(destLat).destLon(destLon)
                .departureAt(departureAt)
                .capacity(4)
                .build();
    }

    @Test
    void 같은_경로_같은_시간이면_최고점() {
        final Ride r = ride(user(1), AIRPORT_LAT, AIRPORT_LON, SEONGSAN_LAT, SEONGSAN_LON, BASE_TIME);

        final RideMatchResult result = rideMatcher.score(r, AIRPORT_LAT, AIRPORT_LON, SEONGSAN_LAT, SEONGSAN_LON, BASE_TIME).orElseThrow();

        assertThat(result.score()).isEqualTo(1.0);
        assertThat(result.originDistanceKm()).isZero();
    }

    @Test
    void 도착지가_멀면_제외() {
        final Ride toAewol = ride(user(1), AIRPORT_LAT, AIRPORT_LON, AEWOL_LAT, AEWOL_LON, BASE_TIME);

        assertThat(rideMatcher.score(toAewol, AIRPORT_LAT, AIRPORT_LON, SEONGSAN_LAT, SEONGSAN_LON, BASE_TIME)).isEmpty();
    }

    @Test
    void 시간차가_범위를_넘으면_제외() {
        final Ride r = ride(user(1), AIRPORT_LAT, AIRPORT_LON, SEONGSAN_LAT, SEONGSAN_LON, BASE_TIME.plusMinutes(61));

        assertThat(rideMatcher.score(r, AIRPORT_LAT, AIRPORT_LON, SEONGSAN_LAT, SEONGSAN_LON, BASE_TIME)).isEmpty();
    }

    @Test
    void 더_가깝고_시간이_맞는_글이_먼저() {
        final User requester = user(99);
        final Ride exact = ride(user(1), AIRPORT_LAT, AIRPORT_LON, SEONGSAN_LAT, SEONGSAN_LON, BASE_TIME);
        final Ride near = ride(user(2), NEAR_AIRPORT_LAT, NEAR_AIRPORT_LON, SEONGSAN_LAT, SEONGSAN_LON, BASE_TIME.plusMinutes(30));

        final List<RideMatchResult> results = rideMatcher.match(
                List.of(near, exact), requester, AIRPORT_LAT, AIRPORT_LON, SEONGSAN_LAT, SEONGSAN_LON, BASE_TIME
        );

        assertThat(results).extracting(RideMatchResult::ride).containsExactly(exact, near);
    }

    @Test
    void 자기_글은_제외() {
        final User requester = user(1);
        final Ride mine = ride(requester, AIRPORT_LAT, AIRPORT_LON, SEONGSAN_LAT, SEONGSAN_LON, BASE_TIME);
        final Ride others = ride(user(2), AIRPORT_LAT, AIRPORT_LON, SEONGSAN_LAT, SEONGSAN_LON, BASE_TIME);

        final List<RideMatchResult> results = rideMatcher.match(
                List.of(mine, others), requester, AIRPORT_LAT, AIRPORT_LON, SEONGSAN_LAT, SEONGSAN_LON, BASE_TIME
        );

        assertThat(results).extracting(RideMatchResult::ride).containsExactly(others);
    }
}
