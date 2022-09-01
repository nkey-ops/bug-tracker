package com.bluesky.bugtraker.view.model.request;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Getter @Setter
public class ProjectRequestModel {

    @NonNull
    @NotEmpty( message = "Field must not be empty")
    @Size(min = 4, max = 30,
            message = "Name length should be between "+ 4 + " and "+ 30 +" characters")
    private String name;
}
