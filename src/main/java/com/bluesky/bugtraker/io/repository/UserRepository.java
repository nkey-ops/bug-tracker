package com.bluesky.bugtraker.io.repository;

import com.bluesky.bugtraker.io.entity.UserEntity;
import com.bluesky.bugtraker.shared.dto.UserDto;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<UserEntity, Long> {
    Optional<UserEntity> findByPublicId(String id);

    Optional<UserEntity> findByEmail(String email);
    boolean existsByEmail(String email);

    boolean existsByPublicId(String id);

    void deleteByPublicId(String id);

}
