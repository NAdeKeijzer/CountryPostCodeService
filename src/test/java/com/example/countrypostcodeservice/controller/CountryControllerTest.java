package com.example.countrypostcodeservice.controller;

import com.example.countrypostcodeservice.dto.CountryInfoResponse;
import com.example.countrypostcodeservice.service.CountryQueryService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.annotation.Resource;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CountryController.class)
class CountryControllerTest {

    @Resource
    MockMvc mockMvc;

    @MockitoBean
    CountryQueryService service;

    @Test
    void getCountry_returns200_withExpectedPayload() throws Exception {
        var dto = new CountryInfoResponse("NL", "Netherlands", "#### @@", "^(\\d{4}[A-Z]{2})$");
        when(service.findOrFetchByCca2("NL")).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/countries/NL"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cca2").value("NL"))
                .andExpect(jsonPath("$.name").value("Netherlands"))
                .andExpect(jsonPath("$.postalFormat").value("#### @@"))
                .andExpect(jsonPath("$.postalRegex").value("^(\\d{4}[A-Z]{2})$"));
    }

    @Test
    void getCountry_returns404_whenNotFound() throws Exception {
        when(service.findOrFetchByCca2(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(get("/countries/XX"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addOrRefresh_returns200_withPayload() throws Exception {
        var dto = new CountryInfoResponse("CO", "Colombia", null, null);
        when(service.refreshFromSource("CO")).thenReturn(Optional.of(dto));

        mockMvc.perform(post("/countries/CO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cca2").value("CO"))
                .andExpect(jsonPath("$.name").value("Colombia"))
                .andExpect(jsonPath("$.postalFormat").doesNotExist()) // null omitted/varies by mapper
                .andExpect(jsonPath("$.postalRegex").doesNotExist()); // (Jackson may include nulls if configured)
    }

    @Test
    void addOrRefresh_returns404_whenUpstream404() throws Exception {
        when(service.refreshFromSource("XX")).thenReturn(Optional.empty());

        mockMvc.perform(post("/countries/XX"))
                .andExpect(status().isNotFound());
    }
    @Test
    void get_Colombia_returns_nullPostalFields() throws Exception {
        var dto = new CountryInfoResponse("CO", "Colombia", null, null);
        when(service.findOrFetchByCca2("CO")).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/countries/CO"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cca2").value("CO"))
                .andExpect(jsonPath("$.name").value("Colombia"))
                .andExpect(jsonPath("$.postalFormat").value(Matchers.nullValue()))
                .andExpect(jsonPath("$.postalRegex").value(Matchers.nullValue()));
    }

    @Test
    void post_Colombia_returns_nullPostalFields_and200() throws Exception {
        var dto = new CountryInfoResponse("CO", "Colombia", null, null);
        when(service.refreshFromSource("CO")).thenReturn(Optional.of(dto));

        mockMvc.perform(post("/countries/CO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cca2").value("CO"))
                .andExpect(jsonPath("$.name").value("Colombia"))
                .andExpect(jsonPath("$.postalFormat").value(Matchers.nullValue()))
                .andExpect(jsonPath("$.postalRegex").value(Matchers.nullValue()));
    }

    @Test
    void get_China_returns_sixDigitRegex() throws Exception {
        var dto = new CountryInfoResponse("CN", "China", "######", "^(\\d{6})$");
        when(service.findOrFetchByCca2("CN")).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/countries/CN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cca2").value("CN"))
                .andExpect(jsonPath("$.name").value("China"))
                .andExpect(jsonPath("$.postalFormat").value("######"))
                .andExpect(jsonPath("$.postalRegex").value("^(\\d{6})$"));

    }

    @Test
    void post_China_returns_sixDigitRegex_and200() throws Exception {
        var dto = new CountryInfoResponse("CN", "China", "######", "^(\\d{6})$");
        when(service.refreshFromSource("CN")).thenReturn(Optional.of(dto));

        mockMvc.perform(post("/countries/CN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cca2").value("CN"))
                .andExpect(jsonPath("$.postalRegex").value("^(\\d{6})$"));
    }
}
