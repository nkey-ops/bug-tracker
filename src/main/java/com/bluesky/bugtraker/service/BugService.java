package com.bluesky.bugtraker.service;


import com.bluesky.bugtraker.shared.dto.BugDto;

import java.util.Set;

public interface BugService {
    BugDto getBugFromProject(String userId, String projectName,  String bugId);
    BugDto createBug(String userId,  String projectName, BugDto map, String reporterId);

    BugDto  updateBug(String userId, String projectName, String bugId, BugDto map);

    void deleteBug(String userId, String projectName, String bugId);

    Set<BugDto> getBugsFromProject(String userId, String projectName, int page, int limit);
}
