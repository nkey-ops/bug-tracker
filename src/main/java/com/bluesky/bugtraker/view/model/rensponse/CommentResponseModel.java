package com.bluesky.bugtraker.view.model.rensponse;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentResponseModel {
  private String publicId;
  private UserResponseModel creator;
  private String content;
  private Date uploadTime;
}
