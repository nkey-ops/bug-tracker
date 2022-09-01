package com.bluesky.bugtraker.shared.dto;

import com.bluesky.bugtraker.io.entity.UserEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class CommentDto {
        private  String publicId;
        private UserDto creator;
        private String content;
        private Date uploadTime;

}
