package com.bluesky.bugtraker.io.entity;

import com.bluesky.bugtraker.shared.ticketstatus.Priority;
import com.bluesky.bugtraker.shared.ticketstatus.Severity;
import com.bluesky.bugtraker.shared.ticketstatus.Status;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

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
  @Column(nullable = false)
  private Status status;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Severity severity;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
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
  private Date createdTime;

  @ManyToOne
  @JoinColumn(
      name = "main_ticket_id",
      referencedColumnName = "id",
      nullable = false,
      updatable = false)
  private TicketEntity mainTicket;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "creator_id", referencedColumnName = "id", nullable = false, updatable = false)
  private UserEntity creator;
}
