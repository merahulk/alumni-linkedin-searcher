package com.example.alumni.dto;

import com.example.alumni.entity.Alumni;

public record AlumniResponse(String name, String currentRole, String university,
                             String location, String linkedinHeadline, Integer passoutYear) {

    public static AlumniResponse from(Alumni a) {
        return new AlumniResponse(a.getName(), a.getCurrentRole(), a.getUniversity(),
                a.getLocation(), a.getLinkedinHeadline(), a.getPassoutYear());
    }
}
