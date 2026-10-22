package org.example.agent.model;

import java.time.LocalDate;

public class BiosInfo {

    private String manufacturer;
    private String version;
    private LocalDate releaseDate;

    public BiosInfo() {
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    @Override
    public String toString() {
        return "BiosInfo{" +
                "manufacturer='" + manufacturer + '\'' +
                ", version='" + version + '\'' +
                ", releaseDate=" + releaseDate +
                '}';
    }
}