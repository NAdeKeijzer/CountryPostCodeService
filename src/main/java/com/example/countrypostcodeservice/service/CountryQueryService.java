// src/main/java/com/example/countrypostcodeservice/service/CountryQueryService.java
package com.example.countrypostcodeservice.service;

import com.example.countrypostcodeservice.domain.Country;
import com.example.countrypostcodeservice.dto.CountryInfoResponse;
import com.example.countrypostcodeservice.repository.CountryRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CountryQueryService {
    private final CountryRepository repo;
    private final CountryImportService importer;

    public CountryQueryService(CountryRepository repo, CountryImportService importer) {
        this.repo = repo;
        this.importer = importer;
    }

    public Optional<CountryInfoResponse> findOrFetchByCca2(String cca2) {
        return repo.findById(cca2.toUpperCase())
                .or(() -> importer.fetchAndSave(cca2))
                .map(this::toDto);
    }

    public Optional<CountryInfoResponse> refreshFromSource(String cca2) {
        return importer.fetchAndSave(cca2).map(this::toDto);
    }

    private CountryInfoResponse toDto(Country c) {
        return new CountryInfoResponse(
                c.getCca2(),
                c.getCommonName(),
                c.getPostalFormat(),
                c.getPostalRegex()
        );
    }
}
