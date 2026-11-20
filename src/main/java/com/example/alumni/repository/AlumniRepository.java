package com.example.alumni.repository;

import com.example.alumni.entity.Alumni;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface AlumniRepository extends JpaRepository<Alumni, Long> {
    List<Alumni> findByProfileUrlIn(Collection<String> profileUrls);
}
