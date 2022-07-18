package com.bluesky.bugtraker.io.repository;

import com.bluesky.bugtraker.io.entity.BugEntity;
import com.bluesky.bugtraker.io.entity.ProjectEntity;
import com.bluesky.bugtraker.io.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface BugRepository  extends CrudRepository<BugEntity, Long>{
    Optional<BugEntity> findByPublicId(String id);

    boolean existsByPublicId(String publicId);

    Page<BugEntity> findAllByProject(ProjectEntity projectEntity, PageRequest of);

     List<BugEntity>  findAllByReporter(UserEntity reporter, PageRequest of);

    boolean existsByProjectAndPublicId(ProjectEntity project, String publicId);
}
