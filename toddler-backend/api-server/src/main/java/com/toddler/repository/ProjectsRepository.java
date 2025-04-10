package com.toddler.repository;

import com.toddler.dto.DashboardDto;
import com.toddler.entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectsRepository extends JpaRepository<ProjectEntity, UUID> {
    @Query(value = """
       select new com.toddler.dto.DashboardDto(p.id, p.name, p.description, pm.role)
       from ProjectEntity p
       join ProjectMemberEntity pm on p.id = pm.projectId
       join UserEntity u on pm.userId = u.id
       where u.email = :email
    """)
    List<DashboardDto> findAllUserProjectsByEmail(@Param("email")String email);
}
