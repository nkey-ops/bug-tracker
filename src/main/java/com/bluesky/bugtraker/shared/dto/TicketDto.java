package com.bluesky.bugtraker.shared.dto;

import com.bluesky.bugtraker.shared.ticketstatus.Priority;
import com.bluesky.bugtraker.shared.ticketstatus.Severity;
import com.bluesky.bugtraker.shared.ticketstatus.Status;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
public class TicketDto implements Serializable {
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

    public TicketDto() {
        this.reportedTime = Date.from(Instant.now());
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TicketDto ticketDto = (TicketDto) o;
        return Objects.equals(id, ticketDto.id) && Objects.equals(publicId, ticketDto.publicId) && status == ticketDto.status && severity == ticketDto.severity && priority == ticketDto.priority && Objects.equals(reportedTime, ticketDto.reportedTime) && Objects.equals(shortDescription, ticketDto.shortDescription) && Objects.equals(howToReproduce, ticketDto.howToReproduce) && Objects.equals(erroneousProgramBehaviour, ticketDto.erroneousProgramBehaviour) && Objects.equals(howToSolve, ticketDto.howToSolve);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, publicId, status, severity, priority, reportedTime, shortDescription, howToReproduce, erroneousProgramBehaviour, howToSolve);
    }
}

