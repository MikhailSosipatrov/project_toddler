package com.toddler.mapper;

import com.toddler.dto.ProjectDto;
import com.toddler.entity.ProjectEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
    ProjectDto fromEntityToDto(ProjectEntity projectEntity);
}
