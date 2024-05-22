package com.bluesky.bugtraker.shared.dto;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentDTO {
  private String publicId;
  private UserDTO creator;
  private String content;
  private Date uploadTime;
}
