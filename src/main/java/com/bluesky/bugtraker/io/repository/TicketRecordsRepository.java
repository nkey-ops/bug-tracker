package com.bluesky.bugtraker.io.repository;

import com.bluesky.bugtraker.io.entity.TicketRecordEntity;
import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRecordsRepository extends DataTablesRepository<TicketRecordEntity, Long> {

    TicketRecordEntity findByPublicId(String recordId);
}
