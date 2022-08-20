package com.bluesky.bugtraker.io.repository;

import com.bluesky.bugtraker.io.entity.authorization.RoleEntity;
import com.bluesky.bugtraker.shared.authorizationenum.Role;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface RoleRepository extends CrudRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByRole(Role role);

    Optional<Set<RoleEntity>> findAllByRoleIn(Set<Role> roles);
}
