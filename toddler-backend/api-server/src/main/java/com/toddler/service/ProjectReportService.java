package com.toddler.service;

import com.toddler.dto.ChartDataDto;
import com.toddler.dto.GeneralStatsDto;
import com.toddler.dto.TasksOverTimeDto;
import com.toddler.dto.TimeChartDataDto;
import com.toddler.repository.ProjectsRepository;
import com.toddler.repository.TaskRepository;
import com.toddler.service.auth.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectReportService {

    private final JwtService jwtService;
    private final ProjectsRepository projectsRepository;
    private final TaskRepository tasksRepository;

    public GeneralStatsDto getGeneralStats(HttpServletRequest request, UUID projectId) {
        validateUserAccess(request, projectId);
        GeneralStatsDto stats = new GeneralStatsDto();
        stats.setProjectName(projectsRepository.findProjectNameById(projectId));
        stats.setTaskCount(tasksRepository.countTasksByProjectId(projectId));
        stats.setMemberCount(projectsRepository.countProjectMembers(projectId));
        stats.setCommentCount(tasksRepository.countCommentsByProjectId(projectId));
        stats.setAvgCompletionTime(tasksRepository.findAvgCompletionTime(projectId) != null ?
                tasksRepository.findAvgCompletionTime(projectId) : 0.0);
        stats.setMaxCompletionTime(tasksRepository.findMaxCompletionTime(projectId) != null ?
                tasksRepository.findMaxCompletionTime(projectId) : 0.0);
        stats.setTotalTimeSpent(tasksRepository.findTotalTimeSpent(projectId) != null ?
                tasksRepository.findTotalTimeSpent(projectId) : 0.0);
        return stats;
    }

    public ChartDataDto getTaskStatusData(HttpServletRequest request, UUID projectId) {
        validateUserAccess(request, projectId);
        Object[][] statusCounts = tasksRepository.findTaskStatusCountsByProjectId(projectId);
        String[] labels = {"TODO", "IN_PROGRESS", "TEST", "DONE"};
        int[] data = new int[4];
        for (Object[] row : statusCounts) {
            String status = (String) row[0];
            Long count = (Long) row[1];
            switch (status) {
                case "TODO":
                    data[0] = count.intValue();
                    break;
                case "IN_PROGRESS":
                    data[1] = count.intValue();
                    break;
                case "TEST":
                    data[2] = count.intValue();
                    break;
                case "DONE":
                    data[3] = count.intValue();
                    break;
            }
        }
        return new ChartDataDto(labels, data);
    }

    public ChartDataDto getTaskPriorityData(HttpServletRequest request, UUID projectId) {
        validateUserAccess(request, projectId);
        Object[][] priorityCounts = tasksRepository.findTaskPriorityCountsByProjectId(projectId);
        String[] labels = {"LOW", "MEDIUM", "HIGH"};
        int[] data = new int[3];
        for (Object[] row : priorityCounts) {
            String priority = (String) row[0];
            Long count = (Long) row[1];
            switch (priority) {
                case "LOW":
                    data[0] = count.intValue();
                    break;
                case "MEDIUM":
                    data[1] = count.intValue();
                    break;
                case "HIGH":
                    data[2] = count.intValue();
                    break;
            }
        }
        return new ChartDataDto(labels, data);
    }

    public TimeChartDataDto getAvgTimeData(HttpServletRequest request, UUID projectId) {
        validateUserAccess(request, projectId);
        Object[][] avgTimes = tasksRepository.findAvgCompletionTimeByPriority(projectId);
        String[] labels = {"LOW", "MEDIUM", "HIGH"};
        double[] data = new double[3];
        for (Object[] row : avgTimes) {
            String priority = (String) row[0];
            BigDecimal avgTime = (BigDecimal) row[1]; // Changed from Double to BigDecimal
            double hours = avgTime != null ? avgTime.doubleValue() : 0.0;
            switch (priority) {
                case "LOW":
                    data[0] = hours / 24; // Convert hours to days
                    break;
                case "MEDIUM":
                    data[1] = hours / 24;
                    break;
                case "HIGH":
                    data[2] = hours / 24;
                    break;
            }
        }
        return new TimeChartDataDto(labels, data);
    }

    public ChartDataDto getUserTasksData(HttpServletRequest request, UUID projectId) {
        validateUserAccess(request, projectId);
        Object[][] userTasks = tasksRepository.findTasksByUser(projectId);
        String[] labels = new String[userTasks.length];
        int[] data = new int[userTasks.length];
        for (int i = 0; i < userTasks.length; i++) {
            labels[i] = (String) userTasks[i][0];
            data[i] = ((Long) userTasks[i][1]).intValue();
        }
        return new ChartDataDto(labels, data);
    }

    public TimeChartDataDto getUserTimeData(HttpServletRequest request, UUID projectId) {
        validateUserAccess(request, projectId);
        Object[][] userTime = tasksRepository.findTimeSpentByUser(projectId);
        String[] labels = new String[userTime.length];
        double[] data = new double[userTime.length];
        for (int i = 0; i < userTime.length; i++) {
            labels[i] = (String) userTime[i][0];
            BigDecimal timeSpent = (BigDecimal) userTime[i][1]; // Changed from Double to BigDecimal
            data[i] = timeSpent != null ? timeSpent.doubleValue() : 0.0;
        }
        return new TimeChartDataDto(labels, data);
    }

    public TasksOverTimeDto getTasksOverTimeData(HttpServletRequest request, UUID projectId) {
        validateUserAccess(request, projectId);
        Object[][] tasksOverTime = tasksRepository.findTasksOverTime(projectId);
        String[] labels = new String[tasksOverTime.length];
        int[] data = new int[tasksOverTime.length];
        for (int i = 0; i < tasksOverTime.length; i++) {
            labels[i] = (String) tasksOverTime[i][0];
            data[i] = ((Long) tasksOverTime[i][1]).intValue();
        }
        return new TasksOverTimeDto(labels, data);
    }

    private void validateUserAccess(HttpServletRequest request, UUID projectId) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid or missing Authorization header");
        }
        String jwtToken = authorizationHeader.substring(7);
        String email = jwtService.getEmailFromToken(jwtToken);
        boolean isMember = projectsRepository.findAllActiveUserProjectsByEmail(email)
                .stream()
                .anyMatch(dto -> dto.getId().equals(projectId)) ||
                projectsRepository.findAllArchiveUserProjectsByEmail(email)
                        .stream()
                        .anyMatch(dto -> dto.getId().equals(projectId));
        if (!isMember) {
            throw new SecurityException("User is not authorized to access this project");
        }
    }
}