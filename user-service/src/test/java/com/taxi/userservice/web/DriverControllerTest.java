package com.taxi.userservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxi.common.dto.DriverRegistrationRequest;
import com.taxi.common.dto.DriverResponse;
import com.taxi.common.dto.DriverStatusUpdateRequest;
import com.taxi.common.model.DriverStatus;
import com.taxi.userservice.service.DriverService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DriverController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class DriverControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    DriverService driverService;

    @Test
    void registerDefaultStatus() throws Exception {
        DriverResponse body = new DriverResponse(
                2L, "Пётр", "petr@test.ru", "+79007654321", "AB1234567", DriverStatus.FREE, Instant.parse("2026-01-02T00:00:00Z"));
        when(driverService.register(any())).thenReturn(body);

        mockMvc.perform(post("/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Пётр\",\"email\":\"petr@test.ru\",\"phone\":\"+79007654321\",\"licenseNumber\":\"AB1234567\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("FREE"));
    }

    @Test
    void listByStatus() throws Exception {
        when(driverService.list(DriverStatus.FREE)).thenReturn(List.of(
                new DriverResponse(1L, "A", "a@test.ru", "1", "L1", DriverStatus.FREE, Instant.now())
        ));

        mockMvc.perform(get("/drivers").param("status", "FREE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("FREE"));
    }

    @Test
    void listAllWhenNoParam() throws Exception {
        when(driverService.list(isNull())).thenReturn(List.of());

        mockMvc.perform(get("/drivers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void patchStatus() throws Exception {
        DriverResponse body = new DriverResponse(
                1L, "A", "a@test.ru", "1", "L1", DriverStatus.BUSY, Instant.now());
        when(driverService.updateStatus(eq(1L), any())).thenReturn(body);

        mockMvc.perform(patch("/drivers/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DriverStatusUpdateRequest(DriverStatus.BUSY))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BUSY"));
    }

    @Test
    void registerValidationFailsOnBlankLicense() throws Exception {
        mockMvc.perform(post("/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new DriverRegistrationRequest("A", "a@test.ru", "1", "", null)
                        )))
                .andExpect(status().isBadRequest());
    }
}
