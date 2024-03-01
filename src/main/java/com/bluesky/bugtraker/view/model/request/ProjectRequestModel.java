package com.bluesky.bugtraker.view.model.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter @Setter
public class ProjectRequestModel {

    @NonNull
    @NotEmpty( message = "Field must not be empty")
    @Size(min = 4, max = 30,
            message = "Name length should be between "+ 4 + " and "+ 30 +" characters")
    private String name;
}
