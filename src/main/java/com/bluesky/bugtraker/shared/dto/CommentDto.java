package com.bluesky.bugtraker.shared.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class CommentDto {
        private  String publicId;
        private UserDto user;
        private String text;
        private Date uploadTime;
}
