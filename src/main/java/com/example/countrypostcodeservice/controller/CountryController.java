package com.example.countrypostcodeservice.controller;

import com.example.countrypostcodeservice.dto.CountryInfoResponse;
import com.example.countrypostcodeservice.service.CountryQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/countries")
public class CountryController {

    private final CountryQueryService service;

    public CountryController(CountryQueryService service) {
        this.service = service;
    }

    /** Get info by country code (serves from DB, fetches if missing) */
    @GetMapping("/{cca2}")
    public ResponseEntity<CountryInfoResponse> getCountry(@PathVariable String cca2) {
        return service.findOrFetchByCca2(cca2)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** Add/refresh from public API and save to DB */
    @PostMapping("/{cca2}")
    public ResponseEntity<CountryInfoResponse> addOrRefresh(@PathVariable String cca2) {
        return service.refreshFromSource(cca2)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
