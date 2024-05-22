package com.bluesky.bugtraker.shared.authorizationenum;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Authority {
  READ_AUTHORITY,
  WRITE_AUTHORITY,
  DELETE_AUTHORITY,
  CREATE_ADMIN_AUTHORITY,
  DELETE_ADMIN_AUTHORITY
}
