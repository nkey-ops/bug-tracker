package com.bluesky.bugtraker.io.repository;

import com.bluesky.bugtraker.io.entity.TicketEntity;
import com.bluesky.bugtraker.io.entity.TicketRecordEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketHistoryRepository extends PagingAndSortingRepository<TicketRecordEntity, Long> {

    Page<TicketRecordEntity> findAllByMainTicket(TicketEntity ticketEntity, Pageable of);

    TicketRecordEntity findByPublicId(String recordId);
}
