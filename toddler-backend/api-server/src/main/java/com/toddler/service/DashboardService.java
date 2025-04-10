package com.toddler.service;

import com.toddler.dto.DashboardDto;
import com.toddler.repository.ProjectsRepository;
import com.toddler.service.auth.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final JwtService jwtService;
    private final ProjectsRepository projectsRepository;

    public List<DashboardDto> getDashboardProjects(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        String jwtToken = authorizationHeader.substring(7);
        String email = jwtService.getEmailFromToken(jwtToken);
        return projectsRepository.findAllUserProjectsByEmail(email);
    }
}
