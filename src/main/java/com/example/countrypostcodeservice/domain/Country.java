// src/main/java/com/example/countrypostcodeservice/domain/Country.java
package com.example.countrypostcodeservice.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "countries")
public class Country {

    @Id
    @Column(length = 2, nullable = false, updatable = false)
    private String cca2;

    @Column(nullable = false)
    private String commonName;

    @Column
    private String postalFormat;

    @Column
    private String postalRegex;

    protected Country() { }

    public Country(String cca2, String commonName, String postalFormat, String postalRegex) {
        if (commonName == null) {
            throw new IllegalArgumentException("commonName cannot be null");
        }
        this.cca2 = cca2 != null ? cca2.toUpperCase() : null;
        this.commonName = commonName;
        this.postalFormat = postalFormat;
        this.postalRegex = postalRegex;
    }

    @PrePersist @PreUpdate
    void normalize() {
        if (cca2 != null) cca2 = cca2.toUpperCase();
        if (commonName == null) throw new IllegalStateException("commonName cannot be null");
    }

    public String getCca2() { return cca2; }
    public String getCommonName() { return commonName; }
    public String getPostalFormat() { return postalFormat; }
    public String getPostalRegex() { return postalRegex; }

}
