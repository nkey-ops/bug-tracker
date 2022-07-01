package com.bluesky.bugtraker.io.repository;

import com.bluesky.bugtraker.io.entity.BugEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface BugRepository  extends CrudRepository<BugEntity, Long>{


    Optional<BugEntity> findByPublicId(String id);
}
