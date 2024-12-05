package fr.exatio.ownergame.commands;

import fr.exatio.ownergame.OwnerGamePlugin;
import fr.exatio.ownergame.apartments.Apartment;
import fr.exatio.ownergame.apartments.ApartmentsConfig;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.HashMap;

public class OwnerGameCommand implements CommandExecutor {

    private OwnerGamePlugin plugin;
    private HashMap<String, Integer> queueLocation = new HashMap<>();
    private HashMap<String, Integer> queueVente = new HashMap<>();
    public OwnerGameCommand(OwnerGamePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        if(command.getName().equals("proposerlocation")) {

            if(strings.length != 2) {
                return false;
            }

            if(Bukkit.getPlayer(strings[0]) == null) {
                commandSender.sendMessage(ChatColor.RED + "Le joueur n'existe pas ou n'est pas en ligne.");
                return false;
            }

            try {
                Integer.parseInt(strings[1]);
            } catch (NumberFormatException e) {
                commandSender.sendMessage(ChatColor.RED + "Le numéro d'appartement spécifié n'est pas un entier.");
                return false;
            }

            if(ApartmentsConfig.getInstance().getBuildingOwnedByPlayer(commandSender.getName()) == -1) {
                commandSender.sendMessage(ChatColor.RED + "Vous devez être un propriétaire d'immeuble pour proposer une location.");
            } else {
                String potentialTenant = strings[0];
                int noApt = Integer.parseInt(strings[1]);

                if(ApartmentsConfig.getInstance().isApartmentOwnedBy(noApt, commandSender.getName())) {
                    commandSender.sendMessage(ChatColor.GREEN + "Une demande a bien été envoyée.");
                    Bukkit.getPlayer(potentialTenant).sendMessage(ChatColor.BLUE + commandSender.getName() + ChatColor.RESET + " vous a proposé une offre de location ! " + ChatColor.GOLD + "Immeuble " + ApartmentsConfig.getInstance().getBuildingOfApartment(noApt) + " appartement n°" + noApt);
                    queueLocation.put(potentialTenant, noApt);
                } else {
                    commandSender.sendMessage(ChatColor.RED + "Vous n'êtes pas propriétaire de l'appartement n°" + noApt);
                }
            }
            return true;

        } else if(command.getName().equals("acceptlocation")) {
            if(queueLocation.containsKey(commandSender.getName())) {
                String buildingOwner = ApartmentsConfig.getInstance().getBuildingOwner(ApartmentsConfig.getInstance().getBuildingOfApartment(queueLocation.get(commandSender.getName())));
                ApartmentsConfig.getInstance().setApartmentTenant(queueLocation.get(commandSender.getName()), commandSender.getName());
                commandSender.sendMessage(ChatColor.GREEN + "Bravo! Vous êtes désormais locataire chez " + buildingOwner + " pour l'appartement n°" + ChatColor.GOLD + queueLocation.get(commandSender.getName()));
                commandSender.sendMessage(ChatColor.DARK_GREEN + "Attention! Si vous n'avez pas les " + ApartmentsConfig.getInstance().getConfig().getInt("prixJournalierAppart") + "$ demandés chaque jour sur votre compte, vous serez automatiquement virés!");
                if(Bukkit.getPlayer(buildingOwner)!=null) Bukkit.getPlayer(buildingOwner).sendMessage(ChatColor.GREEN + commandSender.getName() + " a accepté votre offre de location.");
                queueLocation.remove(commandSender.getName());
            } else {
                commandSender.sendMessage(ChatColor.RED + "Vous n'avez aucune offre à accepter !");
            }
            return true;
        } else if(command.getName().equals("declinelocation")) {

            if(queueLocation.containsKey(commandSender)) {
                String buildingOwner = ApartmentsConfig.getInstance().getBuildingOwner(ApartmentsConfig.getInstance().getBuildingOfApartment(queueLocation.get(commandSender.getName())));
                commandSender.sendMessage(ChatColor.GREEN + "Vous avez bien refusé l'offre de " + buildingOwner);
                if(Bukkit.getPlayer(buildingOwner)!=null) Bukkit.getPlayer(buildingOwner).sendMessage(ChatColor.RED + commandSender.getName() + " a refusé votre offre de location.");
                queueLocation.remove(commandSender.getName());
            } else {
                commandSender.sendMessage(ChatColor.RED + "Vous n'avez aucune offre à refuser !");
            }
            return true;
        } else if(command.getName().equals("proposerimmeuble")) {

            if(strings.length != 1) {
                return false;
            }

            if(Bukkit.getPlayer(strings[0]) == null) {
                commandSender.sendMessage(ChatColor.RED + "Le joueur spécifié n'existe pas ou n'est pas en ligne.");
                return false;
            }

            if(ApartmentsConfig.getInstance().getBuildingOwnedByPlayer(commandSender.getName()) == -1) {
                commandSender.sendMessage(ChatColor.RED + "Vous devez être un propriétaire d'immeuble pour proposer une vente.");
            } else {
                String potentialBuyer = strings[0];

                if(Bukkit.getPlayer(potentialBuyer)!=null) {
                    int noImmeuble = ApartmentsConfig.getInstance().getBuildingOwnedByPlayer(commandSender.getName());
                    commandSender.sendMessage(ChatColor.GREEN + "Une demande a bien été envoyée.");
                    Bukkit.getPlayer(potentialBuyer).sendMessage(ChatColor.BLUE + commandSender.getName() + ChatColor.RESET + " vous a proposé de vendre son : " + ChatColor.GOLD + "Immeuble " + noImmeuble + ChatColor.RESET + " pour " + ApartmentsConfig.getInstance().getBuildings().get(noImmeuble-1).getPrice() + "$");
                    queueVente.put(potentialBuyer, noImmeuble);
                } else {
                    commandSender.sendMessage(ChatColor.RED + "Le joueur " + potentialBuyer + "n'est pas en ligne!");
                }
            }

            return true;

        } else if(command.getName().equals("acceptimmeuble")) {

            if(ApartmentsConfig.getInstance().getBuildingOwnedByPlayer(commandSender.getName()) != -1) {
                commandSender.sendMessage(ChatColor.RED + "Impossible d'accepter la vente. Vous devez d'abord vendre votre propriété actuelle");
            } else {

                if(queueVente.containsKey(commandSender.getName())) {

                    Economy economy = OwnerGamePlugin.getEcon();
                    String buildingOwner = ApartmentsConfig.getInstance().getBuildingOwner(queueVente.get(commandSender.getName()));

                    if(Bukkit.getPlayer(buildingOwner) != null) {

                        int price = ApartmentsConfig.getInstance().getBuildings().get(queueVente.get(commandSender.getName())-1).getPrice();
                        EconomyResponse payment = economy.withdrawPlayer(Bukkit.getOfflinePlayer(commandSender.getName()), price);

                        if(payment.transactionSuccess()) {
                            EconomyResponse withdraw = economy.depositPlayer(Bukkit.getOfflinePlayer(buildingOwner), price);
                            ApartmentsConfig.getInstance().setBuildingOwner(queueVente.get(commandSender.getName()), commandSender.getName());;
                            commandSender.sendMessage(ChatColor.GREEN + "Bravo! Vous êtes désormais propriétaire de :" + ChatColor.GOLD + " Immeuble" + queueVente.get(commandSender.getName()));

                            Bukkit.getPlayer(buildingOwner).sendMessage(ChatColor.GREEN + commandSender.getName() + " a accepté votre offre de vente. Vous n'êtes désormais plus propriétaire.");

                            queueVente.forEach((ppl, numimmeuble) -> {
                                if(numimmeuble == queueVente.get(commandSender.getName()) && !ppl.equals(commandSender.getName())) {
                                    queueVente.remove(ppl);
                                    if(Bukkit.getOfflinePlayer(ppl).isOnline()) Bukkit.getPlayer(ppl).sendMessage(ChatColor.RED + "Votre offre a été suspendue car quelqu'un d'autre a accepté une autre offre pour le même immeuble.");
                                }
                            });

                        } else {
                            commandSender.sendMessage(ChatColor.RED + "Vous n'avez pas assez d'argent pour acheter cet immeuble!");
                            double goldManquant = price - economy.getBalance(Bukkit.getPlayer(commandSender.getName()));
                            commandSender.sendMessage(ChatColor.RED + "Il vous manque " + ChatColor.GOLD + ChatColor.UNDERLINE + goldManquant + ChatColor.RESET + ChatColor.RED + "$");
                        }
                        queueVente.remove(commandSender.getName());

                    } else {
                        commandSender.sendMessage(ChatColor.RED + "Le vendeur est hors ligne, vous ne pouvez pas accepter son offre.");
                    }


                } else {
                    commandSender.sendMessage(ChatColor.RED + "Vous n'avez aucune offre à accepter !");
                }

            }
            return true;

        } else if(command.getName().equals("declineimmeuble")) {

            if(queueVente.containsKey(commandSender)) {
                String buildingOwner = ApartmentsConfig.getInstance().getBuildingOwner(queueVente.get(commandSender.getName()));
                commandSender.sendMessage(ChatColor.GREEN + "Vous avez bien refusé l'offre de " + buildingOwner);
                if(Bukkit.getPlayer(buildingOwner)!=null) Bukkit.getPlayer(buildingOwner).sendMessage(ChatColor.RED + commandSender.getName() + " a refusé votre offre de vente.");
                queueVente.remove(commandSender.getName());
            } else {
                commandSender.sendMessage(ChatColor.RED + "Vous n'avez aucune offre à refuser !");
            }

            return true;

        } else if(command.getName().equals("tpapartment")) {

            int noImmeuble = ApartmentsConfig.getInstance().getTenantApartment(commandSender.getName());
            if(noImmeuble != -1) {
                if (((Player) commandSender).teleport(ApartmentsConfig.getInstance().getApartments().get(noImmeuble-1).getLocation())) {
                    commandSender.sendMessage(ChatColor.GREEN + "Téléportation réussie!");
                } else {
                    commandSender.sendMessage(ChatColor.GREEN + "Téléportation échouée... ce n'est pas normal");
                }
            } else {
                commandSender.sendMessage(ChatColor.RED + "Vous n'êtes pas locataire!");
            }
            return true;

        } else if(command.getName().equals("infosimmeuble")) {

            int owned = ApartmentsConfig.getInstance().getBuildingOwnedByPlayer(commandSender.getName());
            if(owned == -1) {
                commandSender.sendMessage(ChatColor.RED + "Vous n'êtes pas propriétaire d'immeuble !");
            } else {
                commandSender.sendMessage(ChatColor.GREEN + "Vous êtes propriétaire de l'immeuble " + ChatColor.GOLD + owned + ChatColor.GREEN + " qui contient les appartements " + ChatColor.GOLD + ApartmentsConfig.getInstance().getBuildings().get(owned-1).getApartmentsInTheBuilding().toString());
            }
            return true;

        } else if(command.getName().equals("infoslocation")) {

            int tenantOf = ApartmentsConfig.getInstance().getTenantApartment(commandSender.getName());
            if(tenantOf == -1) {
                commandSender.sendMessage(ChatColor.RED + "Vous n'êtes pas locataire !");
            } else {
                commandSender.sendMessage(ChatColor.GREEN + "Vous êtes locataire de l'appartement " + ChatColor.GOLD + tenantOf + ChatColor.GREEN + " immeuble " + ChatColor.GOLD + ApartmentsConfig.getInstance().getBuildingOfApartment(tenantOf) + ChatColor.GREEN + " chez le propriétaire " + ApartmentsConfig.getInstance().getBuildingOwner(ApartmentsConfig.getInstance().getBuildingOfApartment(tenantOf)));
            }
            return true;

        }

        return false;
    }

}
