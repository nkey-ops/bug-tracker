package com.bluesky.bugtraker.io.repository;

import com.bluesky.bugtraker.io.entity.ProjectEntity;
import com.bluesky.bugtraker.io.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends DataTablesRepository<UserEntity, Long> {
    Optional<UserEntity> findByPublicId(String id);

    Optional<UserEntity> findByEmail(String email);
    boolean existsByEmail(String email);

    boolean existsByPublicId(String id);

    void deleteByPublicId(String id);


    Set<UserEntity> findALlBySubscribedToProjectsIn(Set<ProjectEntity> projects);
}
