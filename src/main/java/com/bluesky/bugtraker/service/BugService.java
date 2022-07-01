package com.bluesky.bugtraker.service;


import com.bluesky.bugtraker.shared.dto.BugDto;
import com.bluesky.bugtraker.shared.dto.UserDto;
import org.springframework.stereotype.Service;

public interface BugService {
    BugDto getBug(String id);
    BugDto create(BugDto bugDto, UserDto reporter);
    BugDto create(BugDto bugDto, String userId);

}
