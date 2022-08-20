package com.bluesky.bugtraker.service;


import com.bluesky.bugtraker.shared.dto.CommentDto;
import com.bluesky.bugtraker.shared.dto.TicketDto;
import com.bluesky.bugtraker.shared.dto.UserDto;
import org.springframework.data.domain.Page;

import java.util.Set;

public interface TicketService {
    TicketDto getTicket(String userId, String projectName, String bugId);
    Set<TicketDto> getTickets(String userId, String projectName, int page, int limit);

    TicketDto createTicket(String userId, String projectName, TicketDto map, String reporterId);

    void updateBug(String userId, String projectName, String bugId, TicketDto map);

    void deleteBug(String userId, String projectName, String bugId);

    void addBugFixer(String userId, String projectName,  String bugId, String fixerId);
    void removeBugFixer(String userId, String projectName,  String bugId, String fixerId);

    Set<UserDto> getBugFixers(String userId, String projectName, String bugId, int page, int limit);

    Page<CommentDto> getComments(String userId, String projectName, String tickerId, int page, int limit, String sortBy, String order);

    void createComment(String userId, String projectName, String ticketId, CommentDto comment);
}
