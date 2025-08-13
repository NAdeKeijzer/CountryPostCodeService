package com.example.countrypostcodeservice.dto;

public record CountryInfoResponse(
        String cca2,
        String name,
        String postalFormat,
        String postalRegex
) {}
