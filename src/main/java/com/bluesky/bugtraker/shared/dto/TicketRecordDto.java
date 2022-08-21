package com.bluesky.bugtraker.shared.dto;

import com.bluesky.bugtraker.shared.ticketstatus.Priority;
import com.bluesky.bugtraker.shared.ticketstatus.Severity;
import com.bluesky.bugtraker.shared.ticketstatus.Status;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Getter
@Setter
public class TicketRecordDto implements Serializable {
    @Serial
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private static final long serialVersionUID = 8191538905244886046L;
    private Long id;
    private String publicId;
    private Status status;
    private Severity severity;
    private Priority priority;
    private Date reportedTime;
    private Date lastUpdateTime;
    private ProjectDto project;

    private String shortDescription;
    private String howToReproduce;
    private String erroneousProgramBehaviour;
    private String howToSolve;
    private UserDto reporter;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TicketRecordDto that = (TicketRecordDto) o;
        return Objects.equals(id, that.id) && Objects.equals(publicId, that.publicId) && status == that.status && severity == that.severity && priority == that.priority && Objects.equals(reportedTime, that.reportedTime) && Objects.equals(lastUpdateTime, that.lastUpdateTime) && Objects.equals(project, that.project) && Objects.equals(shortDescription, that.shortDescription) && Objects.equals(howToReproduce, that.howToReproduce) && Objects.equals(erroneousProgramBehaviour, that.erroneousProgramBehaviour) && Objects.equals(howToSolve, that.howToSolve) && Objects.equals(reporter, that.reporter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, publicId, status, severity, priority, reportedTime, lastUpdateTime, project, shortDescription, howToReproduce, erroneousProgramBehaviour, howToSolve, reporter);
    }
}
