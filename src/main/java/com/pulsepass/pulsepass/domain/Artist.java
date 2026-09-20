package com.pulsepass.pulsepass.domain;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "artists")
public class Artist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String stageName;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false)
    private String genre;

    @Column(nullable = false)
    private Boolean active;

    @ManyToMany(mappedBy = "artists")
    private Set<Event> events = new HashSet<>();

}