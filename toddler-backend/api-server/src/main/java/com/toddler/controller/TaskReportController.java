package com.toddler.controller;

import com.toddler.dto.CommentDto;
import com.toddler.dto.StatusHistoryDto;
import com.toddler.dto.TaskReportDto;
import com.toddler.dto.TimeLogDto;
import com.toddler.service.TaskService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tasks/report")
public class TaskReportController {
    private final TaskService taskService;

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskReportDto> getTaskDetails(@PathVariable UUID taskId, HttpServletRequest request) {
        try {
            return ResponseEntity.ok(taskService.getTaskDetails(taskId, request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{taskId}/status-history")
    public ResponseEntity<List<StatusHistoryDto>> getStatusHistory(@PathVariable UUID taskId, HttpServletRequest request) {
        try {
            return ResponseEntity.ok(taskService.getStatusHistory(taskId, request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{taskId}/time-logs")
    public ResponseEntity<List<TimeLogDto>> getTimeLogs(@PathVariable UUID taskId, HttpServletRequest request) {
        try {
            return ResponseEntity.ok(taskService.getTimeLogs(taskId, request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{taskId}/comments")
    public ResponseEntity<List<CommentDto>> getComments(@PathVariable UUID taskId, HttpServletRequest request) {
        try {
            return ResponseEntity.ok(taskService.getComments(taskId, request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}