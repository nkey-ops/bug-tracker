package com.bluesky.bugtraker.view.model.rensponse;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.RepresentationModel;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Getter @Setter
public class ProjectResponseModel extends RepresentationModel<ProjectResponseModel> {
    private String name;

    private String publicId;
    private Set<TicketResponseModel> bugs = new LinkedHashSet<>();
    private Set<UserResponseModel> subscribers = new LinkedHashSet<>();
    private UserResponseModel creator;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ProjectResponseModel that = (ProjectResponseModel) o;
        return Objects.equals(name, that.name) && Objects.equals(bugs, that.bugs) && Objects.equals(subscribers, that.subscribers) && Objects.equals(creator, that.creator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, bugs, subscribers, creator);
    }
}
