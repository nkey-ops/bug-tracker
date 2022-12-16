package com.bluesky.bugtraker.view.model.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Getter @Setter
public class CommentRequestModel {
    @NotEmpty
    @Max(value = 1000, 
            message = "Comment message should be no longer than 1000 symbols")
    private String content;
}
