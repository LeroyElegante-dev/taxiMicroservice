package com.taxi.tripservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxi.common.dto.GeoPointRequest;
import com.taxi.common.dto.TripCreateRequest;
import com.taxi.common.dto.TripResponse;
import com.taxi.common.model.TripStatus;
import com.taxi.tripservice.service.TripService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TripController.class)
class TripControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    TripService tripService;

    @Test
    void createTripReturns201() throws Exception {
        TripCreateRequest body = new TripCreateRequest(
                1L,
                new GeoPointRequest(new BigDecimal("55.75"), new BigDecimal("37.62")),
                new GeoPointRequest(new BigDecimal("55.76"), new BigDecimal("37.63"))
        );
        TripResponse response = new TripResponse(
                10L,
                1L,
                2L,
                TripStatus.ASSIGNED,
                new com.taxi.common.dto.GeoPointResponse(body.origin().latitude(), body.origin().longitude()),
                new com.taxi.common.dto.GeoPointResponse(
                        body.destination().latitude(),
                        body.destination().longitude()
                ),
                new BigDecimal("120.50"),
                null,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z")
        );
        when(tripService.createTrip(any())).thenReturn(response);

        mockMvc.perform(post("/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("ASSIGNED"));
    }

    @Test
    void getTripReturns200() throws Exception {
        TripResponse response = new TripResponse(
                10L,
                1L,
                2L,
                TripStatus.ASSIGNED,
                new com.taxi.common.dto.GeoPointResponse(new BigDecimal("55.75"), new BigDecimal("37.62")),
                new com.taxi.common.dto.GeoPointResponse(new BigDecimal("55.76"), new BigDecimal("37.63")),
                new BigDecimal("120.50"),
                null,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z")
        );
        when(tripService.getById(10L)).thenReturn(response);

        mockMvc.perform(get("/trips/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.passengerId").value(1));
    }
}
