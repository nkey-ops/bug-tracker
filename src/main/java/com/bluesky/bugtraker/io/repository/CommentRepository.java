package com.bluesky.bugtraker.io.repository;


import com.bluesky.bugtraker.io.entity.CommentEntity;
import com.bluesky.bugtraker.io.entity.TicketEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends PagingAndSortingRepository<CommentEntity, Long> {

    Page<CommentEntity> findAllByTicket(TicketEntity ticketEntity, Pageable pageable);
}
