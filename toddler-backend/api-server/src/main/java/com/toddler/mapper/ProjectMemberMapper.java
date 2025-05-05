package com.toddler.mapper;

import com.toddler.dto.ProjectMemberDto;
import com.toddler.dto.UserDto;
import com.toddler.entity.ProjectMemberEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ProjectMemberMapper {
    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "projectId", source = "entity.projectId")
    @Mapping(target = "user", source = "userDto")
    @Mapping(target = "role", source = "entity.role", qualifiedByName = "mapRoleToFrontend")
    ProjectMemberDto fromEntityToDto(ProjectMemberEntity entity, UserDto userDto);

    @Named("mapRoleToFrontend")
    default String mapRoleToFrontend(String backendRole) {
        if (backendRole == null) return null;
        return switch (backendRole) {
            case "WORKER" -> "MEMBER";
            case "MANAGER" -> "ADMIN";
            default -> backendRole;
        };
    }

    @Named("mapRoleToBackend")
    default String mapRoleToBackend(String frontendRole) {
        if (frontendRole == null) return null;
        return switch (frontendRole) {
            case "MEMBER" -> "WORKER";
            case "ADMIN" -> "MANAGER";
            default -> frontendRole;
        };
    }
}
