package com.bluesky.bugtraker.service;


import com.bluesky.bugtraker.shared.dto.CommentDto;
import com.bluesky.bugtraker.shared.dto.TicketDto;
import com.bluesky.bugtraker.shared.dto.TicketRecordDto;
import com.bluesky.bugtraker.shared.dto.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.Set;

public interface TicketService {

    TicketDto getTicket(String bugId);

    void createTicket(String userId, String projectName, TicketDto map, String reporterId);

    void updateTicket(String userId, String projectName, String bugId, TicketDto map);

    Set<TicketDto> getTickets(String userId, String projectName, int page, int limit);

    void deleteBug(String userId, String projectName, String bugId);

    void addAssignedDev(String userId, String projectName);
    Page<UserDto> getAssignedDevs(String ticketId, int page, int limit);

    void removeAssignedDev(String ticketId, String assignedDevId);

    
    void createComment(String ticketId, String creatorId, CommentDto comment);

    Page<CommentDto> getComments(String tickerId, int page, int limit, String sortBy, Sort.Direction dir);


    Page<TicketRecordDto> getTicketRecords(String ticketId, int page, int limit);

    TicketRecordDto getTicketRecord (String recordId);

}
