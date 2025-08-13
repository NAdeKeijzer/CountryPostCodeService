package com.example.countrypostcodeservice.service;

import com.example.countrypostcodeservice.domain.Country;
import com.example.countrypostcodeservice.exception.CountryImportException;
import com.example.countrypostcodeservice.repository.CountryRepository;
import org.springframework.http.HttpStatus;
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
        String path = "/v3.1/alpha/" + code + "?fields=name,postalCode,cca2";

        RestCountry[] response;
        try {
            response = client.get()
                    .uri(path)
                    .retrieve()
                    .bodyToMono(RestCountry[].class)
                    .block();
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) return Optional.empty();
            throw new CountryImportException("REST Countries returned " + e.getStatusCode().value()
                    + " for " + path, e);
        } catch (Exception e) {
            throw new CountryImportException("Failed calling REST Countries: " + e.getMessage(), e);
        }


        if (response == null || response.length == 0) return Optional.empty();

        RestCountry rc = response[0];
        String cca2 = rc.cca2 != null ? rc.cca2.toUpperCase() : code.toUpperCase();
        String common = rc.name != null ? rc.name.common : null;
        String fmt = rc.postalCode != null ? rc.postalCode.format : null;
        String regex = rc.postalCode != null ? rc.postalCode.regex : null;

        if (common == null) return Optional.empty();

        Country country = new Country(cca2, common, fmt, regex);
        return Optional.of(repo.save(country));
    }
}
