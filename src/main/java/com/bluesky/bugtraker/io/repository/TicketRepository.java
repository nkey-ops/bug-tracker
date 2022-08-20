package com.bluesky.bugtraker.io.repository;

import com.bluesky.bugtraker.io.entity.ProjectEntity;
import com.bluesky.bugtraker.io.entity.TicketEntity;
import com.bluesky.bugtraker.io.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface TicketRepository extends CrudRepository<TicketEntity, Long> {
    Optional<TicketEntity> findByPublicId(String id);
    boolean existsByPublicId(String publicId);

    Page<TicketEntity> findAllByReporter(UserEntity reporter, Pageable pageable);

    boolean existsByProjectAndPublicId(ProjectEntity project, String publicId);

    Set<TicketEntity> findAllByProject(ProjectEntity projectEntity);

    Page<TicketEntity> findAllByTicketFixersIn(Set<UserEntity> ticketFixers, Pageable pageable);
}
