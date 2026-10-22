package com.assetmanagement.backend.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BiosData {

    private String manufacturer;

    private String version;

    private LocalDate releaseDate;
}