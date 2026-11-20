package com.example.alumni.controller;

import com.example.alumni.dto.AlumniResponse;
import com.example.alumni.exception.GlobalExceptionHandler;
import com.example.alumni.exception.ScraperException;
import com.example.alumni.service.AlumniService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AlumniController.class)
@Import(GlobalExceptionHandler.class)
class AlumniControllerTest {

    @Autowired MockMvc mvc;
    @MockBean AlumniService service;

    @Test
    void searchReturnsSuccessEnvelope() throws Exception {
        when(service.search(any())).thenReturn(List.of(
                new AlumniResponse("John Doe", "Engineer", "XYZ", "NY", "Engineer at Acme", 2020)));

        mvc.perform(post("/api/alumni/search").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"university\":\"XYZ\",\"designation\":\"Engineer\",\"passoutYear\":\"2020\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].name").value("John Doe"));
    }

    @Test
    void missingFieldsReturn400() throws Exception {
        mvc.perform(post("/api/alumni/search").contentType(MediaType.APPLICATION_JSON).content("{\"university\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    void malformedJsonReturns400() throws Exception {
        mvc.perform(post("/api/alumni/search").contentType(MediaType.APPLICATION_JSON).content("{bad"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void scraperFailureReturns502() throws Exception {
        when(service.search(any())).thenThrow(new ScraperException("down"));
        mvc.perform(post("/api/alumni/search").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"university\":\"XYZ\",\"designation\":\"Engineer\"}"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.message").value("down"));
    }

    @Test
    void getAllReturnsList() throws Exception {
        when(service.findAll()).thenReturn(List.of());
        mvc.perform(get("/api/alumni/all")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("success"));
    }
}
