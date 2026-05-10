package com.taxi.userservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxi.common.dto.PassengerRegistrationRequest;
import com.taxi.common.dto.PassengerResponse;
import com.taxi.common.exception.ResourceNotFoundException;
import com.taxi.userservice.service.PassengerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PassengerController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class PassengerControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    PassengerService passengerService;

    @Test
    void registerReturns201AndBody() throws Exception {
        PassengerResponse body = new PassengerResponse(1L, "Иван", "ivan@test.ru", "+79001234567", Instant.parse("2026-01-01T00:00:00Z"));
        when(passengerService.register(any())).thenReturn(body);

        mockMvc.perform(post("/passengers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new PassengerRegistrationRequest("Иван", "ivan@test.ru", "+79001234567")
                        )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("ivan@test.ru"));
    }

    @Test
    void registerValidationError() throws Exception {
        mockMvc.perform(post("/passengers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new PassengerRegistrationRequest("", "bad-email", "")
                        )))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    void getReturns404() throws Exception {
        when(passengerService.getById(99L)).thenThrow(new ResourceNotFoundException("нет"));

        mockMvc.perform(get("/passengers/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
