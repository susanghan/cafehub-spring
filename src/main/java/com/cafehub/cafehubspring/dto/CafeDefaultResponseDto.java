package com.cafehub.cafehubspring.dto;

import com.cafehub.cafehubspring.domain.Cafe;
import com.cafehub.cafehubspring.domain.Photo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@ApiModel(value = "Cafe 응답")
public class CafeDefaultResponseDto {

    @ApiModelProperty(position = 1, required = true, value = "식별자")
    private Long id;

    @ApiModelProperty(position = 2, required = true, value = "카페 이름")
    private String cafeName;

    @ApiModelProperty(position = 3, required = true, value = "카페 위치")
    private String location;

    @ApiModelProperty(position = 4, required = true, value = "위도")
    private Double latitude;

    @ApiModelProperty(position = 5, required = true, value = "경도")
    private Double longitude;

    @ApiModelProperty(position = 6, value = "콘센트 상태")
    private String plugStatus;

    @ApiModelProperty(position = 7, value = "영업시간 월요일")
    private String monday;

    @ApiModelProperty(position = 8, value = "영업시간 화요일")
    private String tuesday;

    @ApiModelProperty(position = 9, value = "영업시간 수요일")
    private String wednesday;

    @ApiModelProperty(position = 10, value = "영업시간 목요일")
    private String thursday;

    @ApiModelProperty(position = 11, value = "영업시간 금요일")
    private String friday;

    @ApiModelProperty(position = 12, value = "영업시간 토요일")
    private String saturday;

    @ApiModelProperty(position = 13, value = "영업시간 일요일")
    private String sunday;

    @ApiModelProperty(position = 14, value = "포토 url")
    private List<String> photoUrl = new ArrayList<String>();

    public CafeDefaultResponseDto(Cafe cafe) {
        this.id = cafe.getId();
        this.cafeName = cafe.getCafeName();
        this.location = cafe.getLocation();
        this.latitude = cafe.getLatitude();
        this.longitude = cafe.getLongitude();
        this.plugStatus = cafe.getPlugStatus();

        this.monday = cafe.getOpeningHours().getMonday();
        this.tuesday = cafe.getOpeningHours().getTuesday();
        this.wednesday = cafe.getOpeningHours().getWednesday();
        this.thursday = cafe.getOpeningHours().getThursday();
        this.friday = cafe.getOpeningHours().getFriday();
        this.saturday = cafe.getOpeningHours().getSaturday();
        this.sunday = cafe.getOpeningHours().getSunday();

        for(Photo photo : cafe.getPhotos()) {
            this.photoUrl.add(photo.getUrl());
        }
    }
}