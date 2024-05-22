package com.bluesky.bugtraker.view.model.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentRequestModel {
  @NotEmpty
  @Size(max = 1000, message = "Comment message should be no longer than 1000 symbols")
  private String content;
}
