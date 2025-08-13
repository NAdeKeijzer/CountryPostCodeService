package com.example.countrypostcodeservice.repository;

import com.example.countrypostcodeservice.domain.Country;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryRepository extends JpaRepository<Country, String> {}
