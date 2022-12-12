package com.bluesky.bugtraker.view.model.rensponse;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.util.Map;
import java.util.Objects;

@Getter @Setter
public class ProjectResponseModel extends RepresentationModel<ProjectResponseModel> {
    private String name;

    private String publicId;
    
    private UserResponseModel creator;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectResponseModel that = (ProjectResponseModel) o;
        return Objects.equals(name, that.name) && Objects.equals(publicId, that.publicId) && Objects.equals(creator, that.creator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, publicId, creator);
    }
}
