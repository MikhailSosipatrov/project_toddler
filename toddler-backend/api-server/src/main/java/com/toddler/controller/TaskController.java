package com.toddler.controller;

import com.toddler.controller.payload.CreateTaskCommentPayload;
import com.toddler.controller.payload.CreateTaskPayload;
import com.toddler.controller.payload.UpdateTaskPayload;
import com.toddler.controller.payload.UpdateTaskStatusPayload;
import com.toddler.dto.TaskCommentDto;
import com.toddler.dto.TaskDetailsDto;
import com.toddler.dto.TaskDto;
import com.toddler.exception.NotFoundException;
import com.toddler.exception.UnauthorizedException;
import com.toddler.service.TaskCommentService;
import com.toddler.service.TaskService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class TaskController {
    private final TaskService taskService;
    private final TaskCommentService taskCommentService;

    @PostMapping("/projects/{projectId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<TaskDto> createTask(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateTaskPayload payload,
            HttpServletRequest request) {
        TaskDto task = taskService.createTask(projectId, payload, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }

    @GetMapping("/tasks/{taskId}/time/active")
    public ResponseEntity<Map<String, Long>> getActiveTimerElapsedTime(
            @PathVariable UUID taskId,
            HttpServletRequest request) {
        long elapsedTime = taskService.getActiveTimerElapsedTime(taskId, request);
        return ResponseEntity.ok(Map.of("elapsedTime", elapsedTime));
    }

    @PutMapping("/tasks/{taskId}/status")
    public ResponseEntity<TaskDto> updateTaskStatus(
            @PathVariable UUID taskId,
            @Valid @RequestBody UpdateTaskStatusPayload payload,
            HttpServletRequest request) {
        TaskDto task = taskService.updateTaskStatus(taskId, payload, request);
        return ResponseEntity.ok(task);
    }

    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<TaskDetailsDto> getTaskDetails(@PathVariable UUID taskId) {
        TaskDetailsDto task = taskService.getTaskDetails(taskId);
        return ResponseEntity.ok(task);
    }

    @PutMapping("/tasks/{taskId}")
    public ResponseEntity<TaskDetailsDto> updateTask(
            @PathVariable UUID taskId,
            @Valid @RequestBody UpdateTaskPayload payload,
            HttpServletRequest request) {
        TaskDetailsDto task = taskService.updateTask(taskId, payload, request);
        return ResponseEntity.ok(task);
    }

    @PutMapping("/tasks/{taskId}/archive")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Void> archiveTask(
            @PathVariable UUID taskId,
            HttpServletRequest request) {
        taskService.archiveTask(taskId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/tasks/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteTask(
            @PathVariable UUID taskId,
            HttpServletRequest request) {
        taskService.deleteTask(taskId, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/tasks/{taskId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<TaskCommentDto> addComment(
            @PathVariable UUID taskId,
            @Valid @RequestBody CreateTaskCommentPayload payload,
            HttpServletRequest request) {
        TaskCommentDto comment = taskCommentService.addComment(taskId, payload, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @PostMapping("/tasks/{taskId}/time/start")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Void> startTimer(
            @PathVariable UUID taskId,
            HttpServletRequest request) {
        taskService.startTimer(taskId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/tasks/{taskId}/time/stop")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Void> stopTimer(
            @PathVariable UUID taskId,
            HttpServletRequest request) {
        taskService.stopTimer(taskId, request);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<String> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }
}