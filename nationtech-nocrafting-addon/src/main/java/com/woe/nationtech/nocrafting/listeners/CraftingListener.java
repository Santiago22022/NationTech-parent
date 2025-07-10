package com.woe.nationtech.nocrafting.listeners;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.woe.nationtech.api.NationTechAPI;
import com.woe.nationtech.data.Technology;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

public class CraftingListener implements Listener {

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        if (event.getRecipe() == null) {
            return;
        }

        Recipe recipe = event.getRecipe();
        if (!(event.getView().getPlayer() instanceof Player player)) {
            return;
        }

        for (Technology tech : NationTechAPI.getAllTechnologies()) {
            if (tech.recipeKeys().contains(recipe.getResult().getType().getKey().toString())) {
                Resident resident = TownyAPI.getInstance().getResident(player);
                if (resident == null ||!resident.hasTown()) {
                    event.getInventory().setResult(new ItemStack(Material.AIR));
                    return;
                }

                try {
                    Nation nation = resident.getTown().getNation();
                    if (!NationTechAPI.hasTechnology(nation, tech.id())) {
                        event.getInventory().setResult(new ItemStack(Material.AIR));
                        return;
                    }
                } catch (NotRegisteredException e) {
                    event.getInventory().setResult(new ItemStack(Material.AIR));
                    return;
                }
            }
        }
    }
}