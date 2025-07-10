package com.woe.nationtech.ui;

import com.frengor.ultimateadvancementapi.AdvancementTab;
import com.frengor.ultimateadvancementapi.UltimateAdvancementAPI;
import com.frengor.ultimateadvancementapi.advancement.Advancement;
import com.frengor.ultimateadvancementapi.advancement.RootAdvancement;
import com.frengor.ultimateadvancementapi.advancement.display.AdvancementDisplay;
import com.frengor.ultimateadvancementapi.advancement.display.AdvancementFrameType;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.woe.nationtech.NationTech;
import com.woe.nationtech.data.NationData;
import com.woe.nationtech.data.NationDataManager;
import com.woe.nationtech.data.Technology;
import com.woe.nationtech.data.TechnologyManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class AdvancementUIManager {

    private final NationTech plugin;
    private final TechnologyManager technologyManager;
    private final NationDataManager nationDataManager;
    private final AdvancementTab techTab;

    public AdvancementUIManager(NationTech plugin) {
        this.plugin = plugin;
        this.technologyManager = plugin.getTechnologyManager();
        this.nationDataManager = plugin.getNationDataManager();
        UltimateAdvancementAPI api = UltimateAdvancementAPI.getInstance(plugin);

        this.techTab = api.createAdvancementTab("nationtech");
        createRootAdvancement();
    }

    private void createRootAdvancement() {
        AdvancementDisplay rootDisplay = new AdvancementDisplay(
                Material.KNOWLEDGE_BOOK,
                "Tecnologías de la Nación",
                AdvancementFrameType.TASK,
                false, false, 0, 0,
                "El árbol de progreso de tu nación.");
        RootAdvancement root = new RootAdvancement(techTab, "root", rootDisplay, "textures/gui/advancements/backgrounds/stone.png");
        techTab.registerAdvancements(root);
    }

    public void openTechTree(Player player) {
        techTab.showTab(player);
    }

    public void showTechTree(Player player) {
        techTab.showTab(player);
    }

    public void updateAdvancementsFor(Player player) {
        Resident resident = TownyAPI.getInstance().getResident(player);
        if (resident == null ||!resident.hasTown()) {
            return;
        }
        try {
            Nation nation = resident.getTown().getNation();
            updateAdvancementsForNation(nation);
        } catch (NotRegisteredException ignored) {
        }
    }

    public void updateAdvancementsForNation(Nation nation) {
        NationData nationData = nationDataManager.getNationData(nation.getUUID());
        Collection<Advancement> advancements = technologyManager.getTechnologies().stream()
               .map(tech -> createAdvancementForTech(tech, nationData))
               .collect(Collectors.toList());
        techTab.registerAdvancements(advancements.toArray(new Advancement));

        List<Player> members = TownyAPI.getInstance().getOnlinePlayers(nation);
        for (Player member : members) {
            techTab.updateAdvancementsToPlayer(member);
        }
    }

    private Advancement createAdvancementForTech(Technology tech, NationData nationData) {
        boolean isUnlocked = nationData.hasTechnology(tech.id());
        boolean canUnlock = tech.dependencies().stream().allMatch(nationData::hasTechnology);

        AdvancementFrameType frameType;
        if (isUnlocked) {
            frameType = AdvancementFrameType.TASK;
        } else if (canUnlock) {
            frameType = AdvancementFrameType.GOAL;
        } else {
            frameType = AdvancementFrameType.CHALLENGE;
        }

        List<String> description = new java.util.ArrayList<>(tech.description());
        if (!isUnlocked) {
            description.add("");
            description.add(ChatColor.GOLD + "Coste: " + tech.cost() + " Niveles de XP");
        }

        AdvancementDisplay display = new AdvancementDisplay(tech.icon(), tech.displayName(), frameType, true, true, tech.x(), tech.y(), description.toArray(new String));

        String parentId = tech.dependencies().isEmpty()? "root" : tech.dependencies().get(0);

        Advancement advancement = new Advancement(tech.id(), display, parentId);

        if (isUnlocked) {
            advancement.grant(techTab);
        } else {
            advancement.revoke(techTab);
        }

        return advancement;
    }
}