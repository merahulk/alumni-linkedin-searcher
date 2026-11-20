package com.example.alumni.controller;

import com.example.alumni.dto.AlumniResponse;
import com.example.alumni.dto.AlumniSearchRequest;
import com.example.alumni.dto.ApiResponse;
import com.example.alumni.service.AlumniService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alumni")
public class AlumniController {

    private final AlumniService service;

    public AlumniController(AlumniService service) { this.service = service; }

    @PostMapping("/search")
    public ApiResponse<List<AlumniResponse>> search(@Valid @RequestBody AlumniSearchRequest request) {
        return ApiResponse.success(service.search(request));
    }

    @GetMapping("/all")
    public ApiResponse<List<AlumniResponse>> all() {
        return ApiResponse.success(service.findAll());
    }
}
