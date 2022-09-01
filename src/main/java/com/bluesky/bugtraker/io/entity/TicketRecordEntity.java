package com.bluesky.bugtraker.io.entity;

import com.bluesky.bugtraker.shared.ticketstatus.Priority;
import com.bluesky.bugtraker.shared.ticketstatus.Severity;
import com.bluesky.bugtraker.shared.ticketstatus.Status;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Entity(name = "Ticket_Record")
@Table(name = "ticket_records")
@Getter
@Setter
public class TicketRecordEntity implements Serializable {

    @Serial
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private static final long serialVersionUID = -635273108430160633L;

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

    @Lob
    @Column(nullable = false)
    private String howToReproduce;

    @Lob
    @Column(nullable = false)
    private String erroneousProgramBehaviour;

    @Lob
    @Column(columnDefinition = "varchar(250) default 'Solution is not found.'")
    private String howToSolve;

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date reportedTime;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdateTime;

    @ManyToOne
    @JoinColumn(name = "ticket_id", 
                referencedColumnName = "id", 
                nullable = false, updatable = false)
    private TicketEntity mainTicket;

}

