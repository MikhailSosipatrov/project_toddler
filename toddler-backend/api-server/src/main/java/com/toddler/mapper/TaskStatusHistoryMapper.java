package com.toddler.mapper;

import com.toddler.dto.TaskStatusHistoryDto;
import com.toddler.entity.TaskStatusHistoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface TaskStatusHistoryMapper {
    @Mapping(target = "status", source = "status")
    @Mapping(target = "timestamp", source = "statusEnteredAt")
    @Mapping(target = "assignee", ignore = true)
    TaskStatusHistoryDto fromEntityToDto(TaskStatusHistoryEntity entity);
}
