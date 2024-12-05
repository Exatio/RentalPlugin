package fr.exatio.ownergame;

import fr.exatio.ownergame.apartments.Apartment;
import fr.exatio.ownergame.apartments.ApartmentsConfig;
import fr.exatio.ownergame.apartments.Building;
import fr.exatio.ownergame.commands.OwnerGameCommand;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Date;

public class OwnerGamePlugin extends JavaPlugin {

    private static OwnerGamePlugin plugin;
    private static Economy econ = null;

    @Override
    public void onEnable() {
        plugin = this;
        saveDefaultConfig();
        setupEconomy();
        new ApartmentsConfig(this);
        getLogger().info("[OwnerGamePlugin] - Activation du plugin...");

        new BukkitRunnable() {

            @Override
            public void run() {

                for(Building b : ApartmentsConfig.getInstance().getBuildings()) {
                    if(!b.getOwner().equals("CONSOLE")) {
                        EconomyResponse er = econ.depositPlayer(Bukkit.getOfflinePlayer(b.getOwner()), b.getIncome() * (Double.valueOf("1." + ApartmentsConfig.getInstance().getNumberOfTenantsOfBuilding(b.getNumOfBuilding()))));
                    }
                }

                for(Apartment a : ApartmentsConfig.getInstance().getApartments()) {
                    if(!a.getTenant().equals("NOBODY")) {
                        EconomyResponse er = econ.withdrawPlayer(Bukkit.getOfflinePlayer(a.getTenant()), ApartmentsConfig.getInstance().getConfig().getInt("prixJournalierAppart"));
                        if(!er.transactionSuccess()) {
                            if(Bukkit.getOfflinePlayer(a.getTenant()).isOnline()) {
                                Bukkit.getPlayer(a.getTenant()).sendMessage(ChatColor.RED + "Votre location vous avez été retiré : Vous n'avez pas assez d'argent sur votre compte pour payer le loyer quotidien.");
                            }
                            ApartmentsConfig.getInstance().setApartmentTenant(a.getNumOfApartment(), "NOBODY");

                        }
                    }
                }

            }

        }.runTaskTimer(this, 20 * 15, 20 * 60 * 60 * 24);

        CommandExecutor commandExecutor = new OwnerGameCommand(this);

        String[] commands = {"proposerlocation", "acceptlocation", "declinelocation", "proposerimmeuble", "acceptimmeuble", "declineimmeuble", "tpapartment", "infosimmeuble", "infoslocation"};
        for(String str : commands) {
            getServer().getPluginCommand(str).setExecutor(commandExecutor);
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("[OwnerGamePlugin] - Désactivation du plugin...");
    }


    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public static Economy getEcon() {
        return econ;
    }

}
