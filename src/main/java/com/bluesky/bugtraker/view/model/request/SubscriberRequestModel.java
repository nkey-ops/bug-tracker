package com.bluesky.bugtraker.view.model.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubscriberRequestModel {
  @NotEmpty String publicId;
}
