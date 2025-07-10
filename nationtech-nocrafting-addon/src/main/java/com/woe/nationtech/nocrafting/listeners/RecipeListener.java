package com.woe.nationtech.nocrafting.listeners;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.woe.nationtech.api.NationTechAPI;
import com.woe.nationtech.data.Technology;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

public class RecipeListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        updatePlayerRecipes(event.getPlayer());
    }

    private void updatePlayerRecipes(Player player) {
        Resident resident = TownyAPI.getInstance().getResident(player);
        Set<String> unlockedTechs;

        if (resident!= null && resident.hasTown()) {
            try {
                Nation nation = resident.getTown().getNation();
                unlockedTechs = NationTechAPI.getUnlockedTechnologies(nation);
            } catch (NotRegisteredException e) {
                unlockedTechs = Set.of();
            }
        } else {
            unlockedTechs = Set.of();
        }

        Collection<NamespacedKey> toDiscover = new ArrayList<>();
        Collection<NamespacedKey> toForget = new ArrayList<>();

        for (Technology tech : NationTechAPI.getAllTechnologies()) {
            for (String key : tech.recipeKeys()) {
                NamespacedKey recipeKey = NamespacedKey.fromString(key);
                if (recipeKey!= null) {
                    if (unlockedTechs.contains(tech.id())) {
                        toDiscover.add(recipeKey);
                    } else {
                        toForget.add(recipeKey);
                    }
                }
            }
        }

        player.discoverRecipes(toDiscover);
        player.undiscoverRecipes(toForget);
    }
}