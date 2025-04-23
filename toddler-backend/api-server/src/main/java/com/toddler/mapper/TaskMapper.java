package com.toddler.mapper;

import com.toddler.dto.TaskDto;
import com.toddler.entity.TaskEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TaskMapper {
    TaskDto fromEntityToDto(TaskEntity taskEntity);
}
