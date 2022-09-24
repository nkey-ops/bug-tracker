package com.bluesky.bugtraker.service;


import com.bluesky.bugtraker.shared.dto.CommentDto;
import com.bluesky.bugtraker.shared.dto.TicketDto;
import com.bluesky.bugtraker.shared.dto.TicketRecordDto;
import com.bluesky.bugtraker.shared.dto.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

public interface TicketService {

    TicketDto getTicket(String bugId);

    void createTicket(String projectName, TicketDto ticketDto, String reporterId);

    DataTablesOutput<TicketDto> getTickets(String projectId, DataTablesInput input);

    void updateTicket(String ticketId, TicketDto ticketDtoUpdates, String updatedById);

    void deleteTicket(String ticketId);

    void addAssignedDev(String ticketId, String userId);

    DataTablesOutput<UserDto> getAssignedDevs(String ticketId, DataTablesInput input);

    void removeAssignedDev(String ticketId, String userId);

    void createComment(String ticketId, String creatorId, CommentDto comment);

    Page<CommentDto> getComments(String tickerId, int page, int limit, String sortBy, Sort.Direction dir);

    DataTablesOutput<TicketRecordDto> getTicketRecords(String ticketId, DataTablesInput input);

    TicketRecordDto getTicketRecord (String recordId);
}
