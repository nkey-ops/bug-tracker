package com.bluesky.bugtraker.view.model.rensponse;

import com.bluesky.bugtraker.shared.bugstatus.Priority;
import com.bluesky.bugtraker.shared.bugstatus.Severity;
import com.bluesky.bugtraker.shared.bugstatus.Status;
import com.bluesky.bugtraker.shared.dto.UserDto;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.RepresentationModel;

import java.util.Date;
import java.util.Objects;

@Getter @Setter
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "publicId")
public class BugResponseModel extends RepresentationModel<BugResponseModel> {
    private String publicId;
    private Status status;
    private Severity severity;
    private Priority priority;
    private Date reportedTime;
    @JsonIgnore
    private ProjectResponseModel project;

    private String shortDescription;
    private String howToReproduce;
    private String erroneousProgramBehaviour;
    private String howToSolve = "Solution is not found";

    @JsonIgnore
    private UserResponseModel reportedBy;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BugResponseModel that = (BugResponseModel) o;
        return Objects.equals(publicId, that.publicId) && status == that.status && severity == that.severity && priority == that.priority && Objects.equals(reportedTime, that.reportedTime) && Objects.equals(reportedBy, that.reportedBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(publicId, status, severity, priority, reportedTime, reportedBy);
    }

    @Override
    public String toString() {
        return "BugResponseModel{" +
                "publicId='" + publicId + '\'' +
                ", status=" + status +
                ", severity=" + severity +
                ", priority=" + priority +
                ", reportedTime=" + reportedTime +
                ", reportedBy=" + reportedBy +
                '}';
    }
}
