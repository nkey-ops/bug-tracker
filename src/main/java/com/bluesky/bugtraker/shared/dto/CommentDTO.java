package com.bluesky.bugtraker.shared.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class CommentDTO {
    private String publicId;
    private UserDTO creator;
    private String content;
    private Date uploadTime;

}
