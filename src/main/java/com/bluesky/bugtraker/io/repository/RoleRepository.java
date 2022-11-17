package com.bluesky.bugtraker.io.repository;

import com.bluesky.bugtraker.io.entity.RoleEntity;
import com.bluesky.bugtraker.shared.authorizationenum.Role;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends CrudRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByRole(Role role);

    Optional<RoleEntity> findAllByRole(Role role);
}
