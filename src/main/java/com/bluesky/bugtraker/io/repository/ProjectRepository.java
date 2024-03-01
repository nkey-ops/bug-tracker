package com.bluesky.bugtraker.io.repository;


import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.stereotype.Repository;

import com.bluesky.bugtraker.io.entity.ProjectEntity;
import com.bluesky.bugtraker.io.entity.UserEntity;

@Repository
public interface ProjectRepository extends DataTablesRepository<ProjectEntity, Long> {

    boolean existsByCreatorAndName(UserEntity creator, String name);

    boolean existsByPublicIdAndSubscribersIn(String publicId, Set<UserEntity> subscribers);

    Optional<ProjectEntity> findByPublicId(String projectId);

    boolean existsByPublicId(String publicId);
}
