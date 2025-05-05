package com.toddler.controller;

import com.toddler.dto.ChartDataDto;
import com.toddler.dto.GeneralStatsDto;
import com.toddler.dto.TasksOverTimeDto;
import com.toddler.dto.TimeChartDataDto;
import com.toddler.service.ProjectReportService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/project")
public class ProjectReportController {

    private final ProjectReportService projectReportService;

    @GetMapping("/{projectId}/stats")
    public ResponseEntity<GeneralStatsDto> getGeneralStats(HttpServletRequest request,
                                                           @PathVariable UUID projectId) {
        try {
            return ResponseEntity.ok().body(projectReportService.getGeneralStats(request, projectId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{projectId}/task-status")
    public ResponseEntity<ChartDataDto> getTaskStatus(HttpServletRequest request,
                                                      @PathVariable UUID projectId) {
        try {
            return ResponseEntity.ok().body(projectReportService.getTaskStatusData(request, projectId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{projectId}/task-priority")
    public ResponseEntity<ChartDataDto> getTaskPriority(HttpServletRequest request,
                                                        @PathVariable UUID projectId) {
        try {
            return ResponseEntity.ok().body(projectReportService.getTaskPriorityData(request, projectId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{projectId}/avg-time")
    public ResponseEntity<TimeChartDataDto> getAvgTime(HttpServletRequest request,
                                                       @PathVariable UUID projectId) {
        try {
            return ResponseEntity.ok().body(projectReportService.getAvgTimeData(request, projectId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{projectId}/user-tasks")
    public ResponseEntity<ChartDataDto> getUserTasks(HttpServletRequest request,
                                                     @PathVariable UUID projectId) {
        try {
            return ResponseEntity.ok().body(projectReportService.getUserTasksData(request, projectId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{projectId}/user-time")
    public ResponseEntity<TimeChartDataDto> getUserTime(HttpServletRequest request,
                                                        @PathVariable UUID projectId) {
        try {
            return ResponseEntity.ok().body(projectReportService.getUserTimeData(request, projectId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{projectId}/tasks-over-time")
    public ResponseEntity<TasksOverTimeDto> getTasksOverTime(HttpServletRequest request,
                                                             @PathVariable UUID projectId) {
        try {
            return ResponseEntity.ok().body(projectReportService.getTasksOverTimeData(request, projectId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}

