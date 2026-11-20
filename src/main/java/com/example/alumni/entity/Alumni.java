package com.example.alumni.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "alumni", uniqueConstraints = @UniqueConstraint(columnNames = "profile_url"))
public class Alumni {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "job_role")
    private String currentRole;
    private String university;
    private String location;
    @Column(length = 1000)
    private String linkedinHeadline;
    private Integer passoutYear;
    @Column(name = "profile_url", nullable = false)
    private String profileUrl;

    protected Alumni() { }

    public Alumni(String name, String currentRole, String university, String location,
                  String linkedinHeadline, Integer passoutYear, String profileUrl) {
        this.name = name;
        this.currentRole = currentRole;
        this.university = university;
        this.location = location;
        this.linkedinHeadline = linkedinHeadline;
        this.passoutYear = passoutYear;
        this.profileUrl = profileUrl;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCurrentRole() { return currentRole; }
    public String getUniversity() { return university; }
    public String getLocation() { return location; }
    public String getLinkedinHeadline() { return linkedinHeadline; }
    public Integer getPassoutYear() { return passoutYear; }
    public String getProfileUrl() { return profileUrl; }
}
