package com.bluesky.bugtraker.io.repository;

import com.bluesky.bugtraker.io.entity.authorizationEntity.RoleEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface RoleRepository extends CrudRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByName(String name);
}
