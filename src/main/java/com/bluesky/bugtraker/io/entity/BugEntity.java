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
import java.util.Date;
import java.util.Objects;
import java.util.Set;

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

    @Column(nullable = false)
    private String shortDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Priority priority;

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date reportedTime;

    @ManyToOne(optional = false,
            fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false, updatable = false)
    private UserEntity reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    private ProjectEntity project;


    @Lob
    @Column(nullable = false)
    private String howToReproduce;

    @Lob
    @Column(nullable = false)
    private String erroneousProgramBehaviour;

    @Lob
    @Column(columnDefinition = "varchar(250) default 'Solution is not found.'")
    private String howToSolve;

    @ManyToMany(cascade = CascadeType.DETACH)
    @JoinTable(name = "bug_fixers_bugs",
            joinColumns = {@JoinColumn(name = "bug_id")},
            inverseJoinColumns = {@JoinColumn(name = "user_id")}
    )
    private Set<UserEntity> bugFixers;


    public boolean addBugFixer(UserEntity bugFixer) {
        boolean isAddedBugFixer =
                bugFixers.add(bugFixer);
        boolean isAddedBug =
                bugFixer.getWorkingOnBugs().add(this);

        return isAddedBugFixer && isAddedBug;
    }

    public boolean removeBugFixer(UserEntity bugFixer) {
        boolean isRemovedBugFixer =
                bugFixers.remove(bugFixer);
        boolean isRemovedBug =
                bugFixer.getWorkingOnBugs().remove(this);

        return isRemovedBugFixer && isRemovedBug;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BugEntity bugEntity = (BugEntity) o;
        return Objects.equals(id, bugEntity.id) && Objects.equals(publicId, bugEntity.publicId) && Objects.equals(shortDescription, bugEntity.shortDescription) && status == bugEntity.status && severity == bugEntity.severity && priority == bugEntity.priority && Objects.equals(reportedTime, bugEntity.reportedTime) && Objects.equals(reporter, bugEntity.reporter) && Objects.equals(howToReproduce, bugEntity.howToReproduce) && Objects.equals(erroneousProgramBehaviour, bugEntity.erroneousProgramBehaviour) && Objects.equals(howToSolve, bugEntity.howToSolve);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, publicId, shortDescription, status, severity, priority, reportedTime, reporter, howToReproduce, erroneousProgramBehaviour, howToSolve);
    }
}
