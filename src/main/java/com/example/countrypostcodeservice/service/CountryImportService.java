package com.example.countrypostcodeservice.service;

import com.example.countrypostcodeservice.domain.Country;
import com.example.countrypostcodeservice.exception.CountryImportException;
import com.example.countrypostcodeservice.repository.CountryRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Optional;

@Service
public class CountryImportService {

    private final WebClient client;
    private final CountryRepository repo;

    public CountryImportService(WebClient restCountriesWebClient, CountryRepository repo) {
        this.client = restCountriesWebClient;
        this.repo = repo;
    }

    public Optional<Country> fetchAndSave(String alpha2) {
        if (alpha2 == null || alpha2.isBlank()) return Optional.empty();

        String code = alpha2.trim();

        RestCountry rc;
        try {
            rc = client.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v3.1/alpha/{code}")
                            .queryParam("fields", "name,postalCode,cca2")
                            .build(code))
                    .retrieve()
                    .bodyToMono(RestCountry.class)   // <-- single object
                    .block();
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 404) return Optional.empty();
            throw new CountryImportException(
                    "REST Countries returned " + e.getStatusCode().value() + " for /v3.1/alpha/" + code,
                    e
            );
        } catch (Exception e) {
            throw new CountryImportException("Failed calling REST Countries: " + e.getMessage(), e);
        }

        if (rc == null) return Optional.empty();

        String cca2   = rc.cca2 != null ? rc.cca2.toUpperCase() : code.toUpperCase();
        String common = rc.name != null ? rc.name.common : null;
        String fmt    = rc.postalCode != null ? rc.postalCode.format : null;
        String regex  = rc.postalCode != null ? rc.postalCode.regex : null;

        if (common == null) return Optional.empty();

        Country saved = repo.save(new Country(cca2, common, fmt, regex));
        return Optional.of(saved);
    }
}
