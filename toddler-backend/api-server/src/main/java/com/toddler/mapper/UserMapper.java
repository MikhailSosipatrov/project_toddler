package com.toddler.mapper;

import com.toddler.dto.UserDto;
import com.toddler.entity.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto fromEntityToDto(UserEntity user);
}
