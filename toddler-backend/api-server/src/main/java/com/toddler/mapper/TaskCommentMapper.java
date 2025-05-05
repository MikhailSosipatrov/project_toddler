package com.toddler.mapper;

import com.toddler.dto.TaskCommentDto;
import com.toddler.entity.TaskCommentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface TaskCommentMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "text", source = "content")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "author", ignore = true)
    TaskCommentDto fromEntityToDto(TaskCommentEntity entity);
}