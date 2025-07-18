package com.sprint.deokhugam.domain.notification.mapper;

import com.sprint.deokhugam.domain.notification.dto.data.NotificationDto;
import com.sprint.deokhugam.domain.notification.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    // entity -> dto 매핑
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "review.id", target = "reviewId")
    @Mapping(source = "confirmed", target = "isConfirmed")
    NotificationDto toDto(Notification notification);
}