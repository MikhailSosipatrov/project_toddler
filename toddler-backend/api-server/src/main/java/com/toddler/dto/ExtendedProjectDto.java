package com.toddler.dto;

import com.toddler.repository.ProjectsRepository;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ExtendedProjectDto {
    private ProjectDto project;
    private List<TaskDto> tasks;
    private List<ProjectMemberDto> members;
}
