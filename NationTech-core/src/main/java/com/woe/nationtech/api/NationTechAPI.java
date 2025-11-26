package com.woe.nationtech.api;

import com.palmergames.bukkit.towny.object.Nation;
import com.woe.nationtech.data.NationDataManager;
import com.woe.nationtech.data.Technology;
import com.woe.nationtech.data.TechnologyManager;
import com.woe.nationtech.ui.AdvancementUIManager;

import java.util.Set;

public class NationTechAPI {

    private final TechnologyManager technologyManager;
    private final NationDataManager nationDataManager;
    private final AdvancementUIManager advancementUIManager;
    private static NationTechAPI instance;

    public NationTechAPI(TechnologyManager technologyManager, NationDataManager nationDataManager, AdvancementUIManager advancementUIManager) {
        this.technologyManager = technologyManager;
        this.nationDataManager = nationDataManager;
        this.advancementUIManager = advancementUIManager;
        instance = this;
    }

    public static NationTechAPI get() {
        return instance;
    }

    public boolean hasTechnology(Nation nation, String techId) {
        if (nation == null || techId == null) {
            return false;
        }
        return nationDataManager.getNationData(nation.getUUID()).hasTechnology(techId);
    }

    public Set<String> getUnlockedTechnologies(Nation nation) {
        if (nation == null) {
            return Set.of();
        }
        return nationDataManager.getNationData(nation.getUUID()).getUnlockedTechnologies();
    }

    public Technology getTechnology(String techId) {
        return technologyManager.getTechnology(techId);
    }

    public Set<Technology> getAllTechnologies() {
        return technologyManager.getTechnologies();
    }

    public void unlockTechnology(org.bukkit.entity.Player player, String techId) {
        com.palmergames.bukkit.towny.object.Resident resident = com.palmergames.bukkit.towny.TownyAPI.getInstance().getResident(player);
        if (resident == null || !resident.hasTown()) {
            player.sendMessage(org.bukkit.ChatColor.RED + "You must be in a town to do that.");
            return;
        }
        try {
            Nation nation = resident.getTown().getNation();
            technologyManager.unlockTechnology(player, nation, techId);
        } catch (com.palmergames.bukkit.towny.exceptions.NotRegisteredException e) {
            player.sendMessage(org.bukkit.ChatColor.RED + "Your town is not part of a nation.");
        }
    }

    public TechnologyManager getTechnologyManager() {
        return technologyManager;
    }

    public NationDataManager getNationDataManager() {
        return nationDataManager;
    }

    public AdvancementUIManager getAdvancementUIManager() {
        return advancementUIManager;
    }
}