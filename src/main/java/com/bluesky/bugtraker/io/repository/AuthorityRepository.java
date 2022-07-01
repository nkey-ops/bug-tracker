package com.bluesky.bugtraker.io.repository;

import com.bluesky.bugtraker.io.entity.authorizationEntity.AuthorityEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthorityRepository extends CrudRepository<AuthorityEntity, Long> {
    Optional<AuthorityEntity> findByName(String name);
}
