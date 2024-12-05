package fr.exatio.ownergame.apartments;

import org.bukkit.Location;

import java.util.List;

public class Apartment {

    private final int numOfApartment;
    private String tenant;
    private Location location;

    public Apartment(int numOfApartment, String tenant, Location location) {
        this.numOfApartment = numOfApartment;
        this.tenant = tenant;
        this.location = location;
    }

    public int getNumOfApartment() {
        return numOfApartment;
    }

    public String getTenant() {
        return tenant;
    }

    public Location getLocation() {
        return location;
    }
    public void setTenant(String tenant) {
        this.tenant = tenant;
    }
}
