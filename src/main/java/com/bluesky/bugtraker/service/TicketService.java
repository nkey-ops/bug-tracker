package com.bluesky.bugtraker.service;


import com.bluesky.bugtraker.shared.dto.CommentDTO;
import com.bluesky.bugtraker.shared.dto.TicketDTO;
import com.bluesky.bugtraker.shared.dto.TicketRecordDTO;
import com.bluesky.bugtraker.shared.dto.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

public interface TicketService {

    TicketDTO getTicket(String bugId);

    TicketDTO createTicket(String projectName, TicketDTO ticketDto, String reporterId);

    DataTablesOutput<TicketDTO> getTickets(String projectId, DataTablesInput input);

    void updateTicket(String ticketId, TicketDTO ticketDTOUpdates, String updatedById);

    void deleteTicket(String ticketId);

    void addSubscriber(String ticketId, String userId);

    DataTablesOutput<UserDTO> getSubscribers(String ticketId, DataTablesInput input);

    void removeSubscriber(String ticketId, String userId);

    void createComment(String ticketId, String creatorId, CommentDTO comment);

    Page<CommentDTO> getComments(String tickerId, int page, int limit, String sortBy, Sort.Direction dir);

    DataTablesOutput<TicketRecordDTO> getTicketRecords(String ticketId, DataTablesInput input);

    TicketRecordDTO getTicketRecord (String recordId);

    DataTablesOutput<TicketDTO> getTicketsUserSubscribedTo(String userId, DataTablesInput input);
}
