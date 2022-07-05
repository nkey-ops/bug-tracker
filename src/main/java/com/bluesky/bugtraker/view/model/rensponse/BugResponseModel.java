package com.bluesky.bugtraker.view.model.rensponse;

import com.bluesky.bugtraker.shared.bugstatus.Priority;
import com.bluesky.bugtraker.shared.bugstatus.Severity;
import com.bluesky.bugtraker.shared.bugstatus.Status;
import com.bluesky.bugtraker.shared.dto.UserDto;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Objects;

@Getter @Setter
public class BugResponseModel {
    private String publicId;
    private Status status;
    private Severity severity;
    private Priority priority;
    private Date reportedTime;

    private String shortDescription;
    private String howToReproduce;
    private String erroneousProgramBehaviour;
    private String howToSolve = "Solution is not found";
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
