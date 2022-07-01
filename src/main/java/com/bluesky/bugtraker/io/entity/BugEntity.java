package com.bluesky.bugtraker.io.entity;


import com.bluesky.bugtraker.shared.bugstatus.Priority;
import com.bluesky.bugtraker.shared.bugstatus.Severity;
import com.bluesky.bugtraker.shared.bugstatus.Status;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Entity(name = "Bug")
@Table(name = "bugs")
@Getter
@Setter
public class BugEntity implements Serializable {

    @Serial
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private static final long serialVersionUID = -2462587657644552577L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 30, unique = true)
    private String publicId;

    @Column(nullable = false, length = 10)
    private Status status;

    @Column(nullable = false, length = 10)
    private Severity severity;

    @Column(nullable = false, length = 10)
    private Priority priority;

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date reportedTime;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private UserEntity reportedBy;

    @ManyToMany(mappedBy = "workingOnBugs")
    private List<UserEntity> bugFixers = new ArrayList<>();

    @Lob
    @Column(nullable = false)
    private String howToReproduce;

    @Lob
    @Column(nullable = false)
    private String erroneousProgramBehaviour;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BugEntity bugEntity = (BugEntity) o;
        return id.equals(bugEntity.id)
                && publicId.equals(bugEntity.publicId)
                && reportedTime.equals(bugEntity.reportedTime)
                && reportedBy.equals(bugEntity.reportedBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, publicId, reportedTime, reportedBy);
    }
}
