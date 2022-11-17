package com.bluesky.bugtraker.io.repository;

import com.bluesky.bugtraker.io.entity.TicketEntity;
import com.bluesky.bugtraker.io.entity.UserEntity;
import com.bluesky.bugtraker.shared.ticketstatus.Severity;
import com.bluesky.bugtraker.shared.ticketstatus.Status;
import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;
import java.util.Set;

@Repository
public interface TicketRepository extends DataTablesRepository<TicketEntity, Long> {
    Optional<TicketEntity> findByPublicId(String id);

    boolean existsByPublicId(String publicId);

    boolean existsByPublicIdAndSubscribersIn(String publicId, Set<UserEntity> subscribers);
    
    long countAllByProjectCreator(UserEntity userEntity);
    long countAllByProjectCreatorAndLastUpdateTimeAfter(UserEntity projectCreator, Date lastUpdateTime);
    long countAllByProjectCreatorAndSeverity(UserEntity userEntity, Severity severity);
    long countAllByProjectCreatorAndSeverityAndLastUpdateTimeAfter(UserEntity projectCreator, Severity severity, Date createdTime);
    long countAllByProjectCreatorAndStatus(UserEntity userEntity, Status status);
    long countAllByProjectCreatorAndStatusAndLastUpdateTime(UserEntity projectCreator, Status status, Date lastUpdateTime);
}


