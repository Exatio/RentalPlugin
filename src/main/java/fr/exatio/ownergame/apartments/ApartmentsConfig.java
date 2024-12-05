package fr.exatio.ownergame.apartments;

import fr.exatio.ownergame.OwnerGamePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.ArrayList;
import java.util.logging.Level;

public class ApartmentsConfig {

    private static ApartmentsConfig INSTANCE;
    private OwnerGamePlugin plugin;
    private ArrayList<Building> buildings = new ArrayList<>();
    private ArrayList<Apartment> apartments = new ArrayList<>();


    public ApartmentsConfig(OwnerGamePlugin plugin) {
        this.plugin = plugin;
        INSTANCE = this;
        loadApartments();
    }

    public void loadApartments() {
        for(int i = 0 ; i < getConfig().getInt("numberOfBuildings") ; i++) {
            buildings.add(new Building((i+1), getConfig().getString("buildings." + (i+1) + ".owner"), getConfig().getIntegerList("buildings." + (i+1) + ".apartments"), getConfig().getInt("buildings." + (i+1) + ".price"), getConfig().getInt("buildings." + (i+1) + ".income")));
        }

        for(int i = 0 ; i < getConfig().getInt("numberOfApartments") ; i++) {
            apartments.add(new Apartment(i+1, getConfig().getString("apartments." + (i+1) + ".tenant"), new Location(Bukkit.getWorld("world"), getConfig().getDouble("apartments." + (i+1) + ".location.x"), getConfig().getDouble("apartments." + (i+1) + ".location.y"), getConfig().getDouble("apartments." + (i+1) + ".location.z"), (float) getConfig().getDouble("apartments." + (i+1) + ".location.yaw"), (float) getConfig().getDouble("apartments." + (i+1) + ".location.pitch"))));
        }
    }
    public ArrayList<Apartment> getApartments() {
        return apartments;
    }

    public ArrayList<Building> getBuildings() {
        return buildings;
    }

    public void setBuildingOwner(int buildingNum, String own) {
        this.buildings.get(buildingNum-1).setOwner(own);
        getConfig().set("buildings." + buildingNum + ".owner", own);
        saveConfig();
    }

    public void setApartmentTenant(int apartmentNum, String tenant) {
        for(Apartment a : this.apartments) {
            if(a.getTenant().equals(tenant)) {
                a.setTenant("NOBODY");
                getConfig().set("apartments." + a.getNumOfApartment() + ".tenant", "NOBODY");
                if(Bukkit.getPlayer(tenant)!=null) Bukkit.getPlayer(tenant).sendMessage("Votre ancienne location a été supprimée. Il s'agissait de : "  + ChatColor.GOLD + "Immeuble " + getBuildingOfApartment(a.getNumOfApartment()) + " appartement " + a.getNumOfApartment());
            }
        }
        this.apartments.get(apartmentNum-1).setTenant(tenant);
        getConfig().set("apartments." + apartmentNum + ".tenant", tenant);
        saveConfig();
    }

    public int getBuildingOwnedByPlayer(String str) {
        for(Building b : buildings) {
            if (b.getOwner().equals(str)) return b.getNumOfBuilding();
        }
        return -1;
    }

    public String getBuildingOwner(int building) {
        return buildings.get(building-1).getOwner();
    }

    public int getBuildingOfApartment(int apt) {
        for(Building b : buildings) {
            if (b.getApartmentsInTheBuilding().contains(apt)) return b.getNumOfBuilding();
        }
        return -1;
    }

    public boolean isApartmentOwnedBy(int noApt, String str) {
        if(noApt >= buildings.size()) return false;
        return buildings.get(getBuildingOfApartment(noApt)-1).getOwner().equals(str);
    }

    public int getTenantApartment(String str) {
        for(Apartment a : apartments) {
            if (a.getTenant().equals(str)) return a.getNumOfApartment();
        }
        return -1;
    }

    public FileConfiguration getConfig() {
        return this.plugin.getConfig();
    }

    public void saveConfig() {
        this.plugin.saveConfig();
    }

    public static ApartmentsConfig getInstance() {
        return INSTANCE;
    }

    public int getNumberOfTenantsOfBuilding(int building) {
        int nb = 0;
        for(int a : buildings.get(building-1).getApartmentsInTheBuilding()) {
            if(!apartments.get(a-1).getTenant().equals("NOBODY")) {
                nb += 1;
            }
         }
        return nb;
    }
}
