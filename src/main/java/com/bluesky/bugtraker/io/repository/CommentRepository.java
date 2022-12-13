package com.bluesky.bugtraker.io.repository;


import com.bluesky.bugtraker.io.entity.CommentEntity;
import com.bluesky.bugtraker.io.entity.ProjectEntity;
import com.bluesky.bugtraker.io.entity.TicketEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends PagingAndSortingRepository<CommentEntity, Long> {

    List<CommentEntity> findAllByTicket(TicketEntity ticketEntity);
    Page<CommentEntity> findAllByTicket(TicketEntity ticketEntity, Pageable pageable);

    List<CommentEntity> findAllByProject(ProjectEntity projectEntity);
    Page<CommentEntity> findAllByProject(ProjectEntity projectEntity, Pageable pageable);
    
    
}
