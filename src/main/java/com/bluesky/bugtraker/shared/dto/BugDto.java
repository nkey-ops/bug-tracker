package com.bluesky.bugtraker.shared.dto;

import com.bluesky.bugtraker.shared.bugstatus.Priority;
import com.bluesky.bugtraker.shared.bugstatus.Severity;
import com.bluesky.bugtraker.shared.bugstatus.Status;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
public class BugDto implements Serializable {
    @Serial
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private static final long serialVersionUID = 5211376248151308747L;

    private Long id;
    private String publicId;
    private Status status;
    private Severity severity;
    private Priority priority;
    private Date reportedTime;
    private ProjectDto project;

    private String shortDescription;
    private String howToReproduce;
    private String erroneousProgramBehaviour;
    private String howToSolve;
    private UserDto reporter;
    private Set<UserDto> bugFixers;

    public BugDto() {
        this.reportedTime = Date.from(Instant.now());
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BugDto bugDto = (BugDto) o;
        return Objects.equals(id, bugDto.id) && Objects.equals(publicId, bugDto.publicId) && status == bugDto.status && severity == bugDto.severity && priority == bugDto.priority && Objects.equals(reportedTime, bugDto.reportedTime) && Objects.equals(shortDescription, bugDto.shortDescription) && Objects.equals(howToReproduce, bugDto.howToReproduce) && Objects.equals(erroneousProgramBehaviour, bugDto.erroneousProgramBehaviour) && Objects.equals(howToSolve, bugDto.howToSolve);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, publicId, status, severity, priority, reportedTime, shortDescription, howToReproduce, erroneousProgramBehaviour, howToSolve);
    }
}

