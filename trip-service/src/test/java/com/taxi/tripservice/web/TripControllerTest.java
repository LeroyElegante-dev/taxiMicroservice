package com.taxi.tripservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxi.common.dto.GeoPointRequest;
import com.taxi.common.dto.TripCreateRequest;
import com.taxi.common.dto.TripRatingPatchRequest;
import com.taxi.common.dto.TripResponse;
import com.taxi.common.dto.TripStatusPatchRequest;
import com.taxi.common.model.TripStatus;
import com.taxi.tripservice.service.TripService;
import com.taxi.tripservice.support.InvalidTripOperationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TripController.class)
@Import(GlobalExceptionHandler.class)
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

    @Test
    void listTripsForPassengerReturns200() throws Exception {
        TripResponse r = new TripResponse(
                1L, 5L, 2L, TripStatus.ASSIGNED,
                new com.taxi.common.dto.GeoPointResponse(new BigDecimal("55"), new BigDecimal("37")),
                new com.taxi.common.dto.GeoPointResponse(new BigDecimal("56"), new BigDecimal("38")),
                new BigDecimal("1.00"), null,
                Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-01-01T00:00:00Z")
        );
        when(tripService.listForPassenger(5L)).thenReturn(List.of(r));

        mockMvc.perform(get("/trips").param("passenger_id", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void patchStatusReturns200() throws Exception {
        TripResponse r = new TripResponse(
                1L, 5L, 2L, TripStatus.ACCEPTED,
                new com.taxi.common.dto.GeoPointResponse(new BigDecimal("55"), new BigDecimal("37")),
                new com.taxi.common.dto.GeoPointResponse(new BigDecimal("56"), new BigDecimal("38")),
                new BigDecimal("1.00"), null,
                Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-01-01T00:00:00Z")
        );
        when(tripService.updateStatus(eq(1L), any(TripStatusPatchRequest.class))).thenReturn(r);

        mockMvc.perform(patch("/trips/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ACCEPTED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    void patchStatusInvalidReturns409() throws Exception {
        when(tripService.updateStatus(eq(1L), any(TripStatusPatchRequest.class)))
                .thenThrow(new InvalidTripOperationException("Недопустимый переход статуса: ASSIGNED -> IN_PROGRESS"));

        mockMvc.perform(patch("/trips/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void patchRatingReturns200() throws Exception {
        TripResponse r = new TripResponse(
                1L, 5L, 2L, TripStatus.COMPLETED,
                new com.taxi.common.dto.GeoPointResponse(new BigDecimal("55"), new BigDecimal("37")),
                new com.taxi.common.dto.GeoPointResponse(new BigDecimal("56"), new BigDecimal("38")),
                new BigDecimal("1.00"), 5,
                Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-01-01T00:00:00Z")
        );
        when(tripService.updateRating(eq(1L), any(TripRatingPatchRequest.class))).thenReturn(r);

        mockMvc.perform(patch("/trips/1/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(5));
    }
}
