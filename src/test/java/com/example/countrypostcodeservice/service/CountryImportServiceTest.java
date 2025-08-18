package com.example.countrypostcodeservice.service;

import com.example.countrypostcodeservice.domain.Country;
import com.example.countrypostcodeservice.repository.CountryRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CountryImportServiceTest {

    private MockWebServer server;

    @BeforeEach
    void setup() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void fetchAndSave_mapsAndPersists_Netherlands() throws Exception {
        // given upstream returns a SINGLE OBJECT for NL
        String body = """
            {
              "cca2": "NL",
              "name": { "common": "Netherlands" },
              "postalCode": { "format": "#### @@", "regex": "^(\\\\d{4}[A-Z]{2})$" }
            }
            """;
        server.enqueue(new MockResponse().setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(body));

        WebClient client = WebClient.builder()
                .baseUrl(server.url("/").toString())
                .build();

        CountryRepository repo = mock(CountryRepository.class);
        when(repo.save(any(Country.class))).thenAnswer(inv -> inv.getArgument(0));

        CountryImportService service = new CountryImportService(client, repo);

        // when
        Optional<Country> savedOpt = service.fetchAndSave("nl");

        // then
        assertThat(savedOpt).isPresent();
        Country saved = savedOpt.get();
        assertThat(saved.getCca2()).isEqualTo("NL");
        assertThat(saved.getCommonName()).isEqualTo("Netherlands");
        assertThat(saved.getPostalFormat()).isEqualTo("#### @@");
        assertThat(saved.getPostalRegex()).isEqualTo("^(\\d{4}[A-Z]{2})$");

        // verify request path uses /alpha/{code} and our fields filter
        var req = server.takeRequest();
        assertThat(req.getPath())
                .startsWith("/v3.1/alpha/nl")
                .contains("fields=name,postalCode,cca2");

        verify(repo, times(1)).save(any(Country.class));
    }

    @Test
    void fetchAndSave_returnsEmpty_on404() {
        server.enqueue(new MockResponse().setResponseCode(404));

        WebClient client = WebClient.builder()
                .baseUrl(server.url("/").toString())
                .build();
        CountryRepository repo = mock(CountryRepository.class);

        CountryImportService service = new CountryImportService(client, repo);

        Optional<Country> result = service.fetchAndSave("xx");
        assertThat(result).isEmpty();
        verify(repo, never()).save(any());
    }

    @Test
    void fetchAndSave_returnsEmpty_whenCommonNameMissing() {
        // single object with empty 'name'
        String body = """
            {
              "cca2": "CO",
              "name": {},
              "postalCode": { "format": null, "regex": null }
            }
            """;
        server.enqueue(new MockResponse().setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(body));

        WebClient client = WebClient.builder()
                .baseUrl(server.url("/").toString())
                .build();
        CountryRepository repo = mock(CountryRepository.class);

        CountryImportService service = new CountryImportService(client, repo);

        Optional<Country> result = service.fetchAndSave("co");
        assertThat(result).isEmpty();
        verify(repo, never()).save(any());
    }

    @Test
    void fetchAndSave_maps_Colombia_withNullPostalFields() {
        String body = """
            {
              "cca2": "CO",
              "name": { "common": "Colombia" },
              "postalCode": { "format": null, "regex": null }
            }
            """;
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(body));

        WebClient client = WebClient.builder().baseUrl(server.url("/").toString()).build();
        CountryRepository repo = mock(CountryRepository.class);
        when(repo.save(any(Country.class))).thenAnswer(inv -> inv.getArgument(0));

        var service = new CountryImportService(client, repo);

        Optional<Country> saved = service.fetchAndSave("co");
        assertThat(saved).isPresent();
        var co = saved.get();
        assertThat(co.getCca2()).isEqualTo("CO");
        assertThat(co.getCommonName()).isEqualTo("Colombia");
        assertThat(co.getPostalFormat()).isNull();
        assertThat(co.getPostalRegex()).isNull();

        verify(repo, times(1)).save(any(Country.class));
    }

    @Test
    void fetchAndSave_maps_China_withSixDigitRegex() {
        String body = """
            {
              "cca2": "CN",
              "name": { "common": "China" },
              "postalCode": { "format": "######", "regex": "^(\\\\d{6})$" }
            }
            """;
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(body));

        WebClient client = WebClient.builder().baseUrl(server.url("/").toString()).build();
        CountryRepository repo = mock(CountryRepository.class);
        when(repo.save(any(Country.class))).thenAnswer(inv -> inv.getArgument(0));

        var service = new CountryImportService(client, repo);

        Optional<Country> saved = service.fetchAndSave("cn");
        assertThat(saved).isPresent();
        var cn = saved.get();
        assertThat(cn.getCca2()).isEqualTo("CN");
        assertThat(cn.getCommonName()).isEqualTo("China");
        assertThat(cn.getPostalFormat()).isEqualTo("######");
        assertThat(cn.getPostalRegex()).isEqualTo("^(\\d{6})$");

        verify(repo, times(1)).save(any(Country.class));
    }
}
