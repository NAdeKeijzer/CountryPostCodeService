package com.example.countrypostcodeservice.service;

import com.example.countrypostcodeservice.domain.Country;
import com.example.countrypostcodeservice.dto.CountryInfoResponse;
import com.example.countrypostcodeservice.repository.CountryRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CountryQueryServiceTest {

    @Test
    void findOrFetch_prefersRepository_hit() {
        var repo = mock(CountryRepository.class);
        var importer = mock(CountryImportService.class);

        var nl = new Country("NL", "Netherlands", "#### @@", "^(\\d{4}[A-Z]{2})$");
        when(repo.findById("NL")).thenReturn(Optional.of(nl));

        var service = new CountryQueryService(repo, importer);

        Optional<CountryInfoResponse> dto = service.findOrFetchByCca2("nl");
        assertThat(dto).isPresent();
        assertThat(dto.get().cca2()).isEqualTo("NL");
        assertThat(dto.get().name()).isEqualTo("Netherlands");

        verify(importer, never()).fetchAndSave(anyString());
    }

    @Test
    void findOrFetch_fallsBackToImporter_whenRepoEmpty() {
        var repo = mock(CountryRepository.class);
        var importer = mock(CountryImportService.class);

        when(repo.findById("CO")).thenReturn(Optional.empty());
        var co = new Country("CO", "Colombia", null, null);
        when(importer.fetchAndSave("co")).thenReturn(Optional.of(co));

        var service = new CountryQueryService(repo, importer);

        Optional<CountryInfoResponse> dto = service.findOrFetchByCca2("co");
        assertThat(dto).isPresent();
        assertThat(dto.get().name()).isEqualTo("Colombia");
    }

    @Test
    void findOrFetch_returnsEmpty_whenNeitherHasData() {
        var repo = mock(CountryRepository.class);
        var importer = mock(CountryImportService.class);
        when(repo.findById("XX")).thenReturn(Optional.empty());
        when(importer.fetchAndSave("xx")).thenReturn(Optional.empty());

        var service = new CountryQueryService(repo, importer);
        assertThat(service.findOrFetchByCca2("xx")).isEmpty();
    }

    @Test
    void refreshFromSource_delegatesToImporter() {
        var repo = mock(CountryRepository.class);
        var importer = mock(CountryImportService.class);
        var nl = new Country("NL", "Netherlands", "#### @@", "^(\\d{4}[A-Z]{2})$");
        when(importer.fetchAndSave("nl")).thenReturn(Optional.of(nl));

        var service = new CountryQueryService(repo, importer);

        var dto = service.refreshFromSource("nl");
        assertThat(dto).isPresent();
        assertThat(dto.get().cca2()).isEqualTo("NL");
    }
    @Test
    void findOrFetch_fallsBackToImporter_forChina_whenNotInRepo() {
        var repo = mock(CountryRepository.class);
        var importer = mock(CountryImportService.class);

        when(repo.findById("CN")).thenReturn(Optional.empty());
        var cnEntity = new Country("CN", "China", "######", "^(\\d{6})$");
        when(importer.fetchAndSave("cn")).thenReturn(Optional.of(cnEntity));

        var service = new CountryQueryService(repo, importer);

        Optional<CountryInfoResponse> dto = service.findOrFetchByCca2("cn");
        assertThat(dto).isPresent();
        assertThat(dto.get().cca2()).isEqualTo("CN");
        assertThat(dto.get().name()).isEqualTo("China");
        assertThat(dto.get().postalFormat()).isEqualTo("######");
        assertThat(dto.get().postalRegex()).isEqualTo("^(\\d{6})$");

        verify(importer, times(1)).fetchAndSave("cn");
    }
}
