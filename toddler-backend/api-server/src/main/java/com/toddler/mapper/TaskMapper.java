package com.toddler.mapper;

import com.toddler.dto.CommentDto;
import com.toddler.dto.StatusHistoryDto;
import com.toddler.dto.TaskDetailsDto;
import com.toddler.dto.TaskDto;
import com.toddler.dto.TaskReportDto;
import com.toddler.dto.TimeLogDto;
import com.toddler.entity.TaskCommentEntity;
import com.toddler.entity.TaskEntity;
import com.toddler.entity.TaskStatusHistoryEntity;
import com.toddler.entity.TaskTimeLogsEntity;
import com.toddler.repository.UserRepository;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public abstract class TaskMapper {

    @Autowired
    protected UserRepository userRepository;

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "title")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "priority", source = "priority")
    @Mapping(target = "projectId", source = "projectId")
    @Mapping(target = "deadline", source = "dueDate")
    @Mapping(target = "executor", ignore = true)
    public abstract TaskDto fromEntityToDto(TaskEntity entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "priority", source = "priority")
    @Mapping(target = "projectId", source = "projectId")
    @Mapping(target = "deadline", source = "dueDate")
    @Mapping(target = "assignee", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "statusHistory", ignore = true)
    @Mapping(target = "timeLogs", ignore = true)
    public abstract TaskDetailsDto fromEntityToDetailsDto(TaskEntity entity);

    @Mapping(target = "assigneeName", source = "assignedTo", qualifiedByName = "getUserNameById")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "formatDateTime")
    public abstract TaskReportDto toReportDto(TaskEntity task, @Context UserRepository userRepository);

    @Mapping(target = "statusEnteredAt", source = "statusEnteredAt", qualifiedByName = "formatDateTime")
    @Mapping(target = "statusLeftAt", source = "statusLeftAt", qualifiedByName = "formatDateTime")
    public abstract StatusHistoryDto toDto(TaskStatusHistoryEntity history);

    @Mapping(target = "userName", source = "userId", qualifiedByName = "getUserNameById")
    @Mapping(target = "hours", source = ".", qualifiedByName = "calculateHours")
    @Mapping(target = "startTime", source = "startTime", qualifiedByName = "formatDateTime")
    public abstract TimeLogDto toDto(TaskTimeLogsEntity timeLog, @Context UserRepository userRepository);

    @Mapping(target = "userName", source = "userId", qualifiedByName = "getUserNameById")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "formatDateTime")
    public abstract CommentDto toDto(TaskCommentEntity comment, @Context UserRepository userRepository);

    public abstract List<TaskReportDto> toDtoList(List<TaskEntity> tasks, @Context UserRepository userRepository);
    public abstract List<StatusHistoryDto> toStatusHistoryDtoList(List<TaskStatusHistoryEntity> history);
    public abstract List<TimeLogDto> toTimeLogDtoList(List<TaskTimeLogsEntity> timeLogs, @Context UserRepository userRepository);
    public abstract List<CommentDto> toCommentDtoList(List<TaskCommentEntity> comments, @Context UserRepository userRepository);

    @Named("formatDateTime")
    String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
    }

    @Named("calculateHours")
    double calculateHours(TaskTimeLogsEntity timeLog) {
        if (timeLog.getEndTime() == null || timeLog.getStartTime() == null) {
            return 0.0;
        }
        long diffInMillis = java.time.Duration.between(timeLog.getStartTime(), timeLog.getEndTime()).toMillis();
        return diffInMillis / 1000.0 / 60.0 / 60.0;
    }

    @Named("getUserNameById")
    String getUserNameById(UUID userId, @Context UserRepository userRepository) {
        return userRepository.findById(userId)
                .map(user -> user.getUsername() != null ? user.getUsername() : user.getFirstName() + " " + user.getLastName())
                .orElse("Неизвестно");
    }
}