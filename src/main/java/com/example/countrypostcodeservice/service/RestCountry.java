package com.example.countrypostcodeservice.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
class RestCountry {
    @JsonProperty("cca2")
    public String cca2;

    @JsonProperty("name")
    public Name name;

    @JsonProperty("postalCode")
    public PostalCode postalCode;

    public RestCountry() {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Name {
        @JsonProperty("common")
        public String common;
        public Name() {}
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class PostalCode {
        @JsonProperty("format")
        public String format;
        @JsonProperty("regex")
        public String regex;
        public PostalCode() {}
    }
}
