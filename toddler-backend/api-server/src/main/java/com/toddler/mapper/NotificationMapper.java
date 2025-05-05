package com.toddler.mapper;

import com.toddler.dto.NotificationDto;
import com.toddler.entity.NotificationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationDto toDto(NotificationEntity entity);

    List<NotificationDto> toDtoList(List<NotificationEntity> entities);
}
