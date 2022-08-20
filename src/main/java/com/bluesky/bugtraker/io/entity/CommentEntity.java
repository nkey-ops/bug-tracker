package com.bluesky.bugtraker.io.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Entity(name = "Comment")
@Table(name = "comments")
@Getter @Setter
public class CommentEntity implements Serializable {

    @Serial
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private static final long serialVersionUID = 2807970031426195256L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30, unique = true)
    private String publicId;

    @Column(nullable = false, length = 1000)
    private String text;

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date uploadTime;

    @ManyToOne(optional = false,
            fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private UserEntity user;

    @ManyToOne(
            optional = false,
            fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false, updatable = false)
    private TicketEntity ticket;


}
