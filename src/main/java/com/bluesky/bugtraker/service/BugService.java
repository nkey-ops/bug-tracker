package com.bluesky.bugtraker.service;


import com.bluesky.bugtraker.shared.dto.BugDto;
import com.bluesky.bugtraker.shared.dto.UserDto;

import java.util.Set;

public interface BugService {
    BugDto getBug(String userId, String projectName, String bugId);
    Set<BugDto> getBugs(String userId, String projectName, int page, int limit);

    BugDto createBug(String userId,  String projectName, BugDto map, String reporterId);

    BugDto  updateBug(String userId, String projectName, String bugId, BugDto map);

    void deleteBug(String userId, String projectName, String bugId);

    void addBugFixer(String userId, String projectName,  String bugId, String fixerId);
    void removeBugFixer(String userId, String projectName,  String bugId, String fixerId);

    Set<UserDto> getBugFixers(String userId, String projectName, String bugId, int page, int limit);
}
