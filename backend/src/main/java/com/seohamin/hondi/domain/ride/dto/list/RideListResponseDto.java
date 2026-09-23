package com.seohamin.hondi.domain.ride.dto.list;

import lombok.Getter;

import java.util.List;

@Getter
public class RideListResponseDto {
    private final List<RideListItemResponseDto> rides;
    private final boolean hasNext;

    public RideListResponseDto(
            final List<RideListItemResponseDto> rides,
            final boolean hasNext
    ) {
        this.rides = rides;
        this.hasNext = hasNext;
    }
}
