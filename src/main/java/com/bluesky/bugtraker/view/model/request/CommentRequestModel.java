package com.bluesky.bugtraker.view.model.request;

import com.bluesky.bugtraker.view.model.rensponse.UserResponseModel;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Date;

@Getter @Setter
public class CommentRequestModel {
    private String text;
}
