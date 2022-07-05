package com.bluesky.bugtraker.io.repository;

import com.bluesky.bugtraker.io.entity.authorization.AuthorityEntity;
import com.bluesky.bugtraker.shared.authorizationenum.Authority;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthorityRepository extends CrudRepository<AuthorityEntity, Long> {
    Optional<AuthorityEntity> findByAuthority(Authority authority);
}
