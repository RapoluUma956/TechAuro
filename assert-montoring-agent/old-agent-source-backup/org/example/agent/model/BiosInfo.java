/*
 * Decompiled with CFR 0.152.
 */
package org.example.agent.model;

import java.time.LocalDate;

public class BiosInfo {
    private String manufacturer;
    private String version;
    private LocalDate releaseDate;

    public String getManufacturer() {
        return this.manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public LocalDate getReleaseDate() {
        return this.releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String toString() {
        return "BiosInfo{manufacturer='" + this.manufacturer + "', version='" + this.version + "', releaseDate=" + String.valueOf(this.releaseDate) + "}";
    }
}

