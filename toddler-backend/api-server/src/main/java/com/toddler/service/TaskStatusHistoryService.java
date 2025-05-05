package com.toddler.service;

import com.toddler.dto.TaskStatusHistoryDto;
import com.toddler.entity.TaskEntity;
import com.toddler.entity.TaskStatusHistoryEntity;
import com.toddler.exception.NotFoundException;
import com.toddler.mapper.TaskStatusHistoryMapper;
import com.toddler.repository.TaskRepository;
import com.toddler.repository.TaskStatusHistoryRepository;
import com.toddler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskStatusHistoryService {

    private final TaskStatusHistoryRepository taskStatusHistoryRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskStatusHistoryMapper taskStatusHistoryMapper;

    @Transactional(readOnly = true)
    public List<TaskStatusHistoryDto> getStatusHistoryByTaskId(UUID taskId) {
        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found with ID: " + taskId));

        List<TaskStatusHistoryEntity> history = taskStatusHistoryRepository.findByTaskId(taskId);
        return history.stream()
                .map(historyEntry -> {
                    TaskStatusHistoryDto dto = taskStatusHistoryMapper.fromEntityToDto(historyEntry);
                    if (task.getAssignedTo() != null) {
                        userRepository.findById(task.getAssignedTo())
                                .map(user -> user.getFirstName() + " " + user.getLastName() + "(" + user.getUsername() + ")")
                                .ifPresent(dto::setAssignee);
                    } else {
                        dto.setAssignee("Не назначено");
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
