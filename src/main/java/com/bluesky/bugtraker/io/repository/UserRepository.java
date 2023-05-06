package com.bluesky.bugtraker.io.repository;

import com.bluesky.bugtraker.io.entity.UserEntity;
import com.bluesky.bugtraker.shared.authorizationenum.Role;
import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends DataTablesRepository<UserEntity, Long> {
    Optional<UserEntity> findByPublicId(String id);
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByEmailVerificationToken(String token);
    
    void deleteAllDistinctByEmail(String email);
    void deleteByEmail(String email);
    boolean existsByEmail(String email);
    long countByRoleEntityRole(Role role);
    long deleteAllByEmailNotLike(String email);

}
