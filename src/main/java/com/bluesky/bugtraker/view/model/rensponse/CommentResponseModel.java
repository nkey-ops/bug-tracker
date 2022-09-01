package com.bluesky.bugtraker.view.model.rensponse;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter @Setter
public class CommentResponseModel {
    private String publicId;
    private UserResponseModel creator;
    private String content;
    private Date uploadTime;
}
