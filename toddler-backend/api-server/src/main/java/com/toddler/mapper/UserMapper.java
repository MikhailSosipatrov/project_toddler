package com.toddler.mapper;

import com.toddler.controller.payload.UserProfileUpdateRequest;
import com.toddler.dto.UserDto;
import com.toddler.dto.UserProfileDto;
import com.toddler.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto fromEntityToDto(UserEntity user);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "profilePicture", source = "profilePicture")
    UserProfileDto toProfileDto(UserEntity entity);

    void updateEntityFromRequest(UserProfileUpdateRequest request, @MappingTarget UserEntity entity);
}
