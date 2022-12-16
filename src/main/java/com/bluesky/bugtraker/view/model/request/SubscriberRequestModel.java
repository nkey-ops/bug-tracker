package com.bluesky.bugtraker.view.model.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
public class SubscriberRequestModel {
    @NotEmpty
    String publicId;
}
