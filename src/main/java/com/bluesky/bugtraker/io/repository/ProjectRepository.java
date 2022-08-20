package com.bluesky.bugtraker.io.repository;


import com.bluesky.bugtraker.io.entity.ProjectEntity;
import com.bluesky.bugtraker.io.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface ProjectRepository extends PagingAndSortingRepository<ProjectEntity, Long> {
    Page<ProjectEntity> findAllByCreator(UserEntity userEntity, PageRequest pageRequest);

    Optional<ProjectEntity> findByCreatorAndName(UserEntity creator, String name);

    boolean existsByCreatorAndName(UserEntity creator, String name);

    Set<ProjectEntity> findAllByCreator(UserEntity userEntity);

    Page<ProjectEntity> findAllBySubscribersIn(Set<UserEntity> userEntity, Pageable pageable);
}
