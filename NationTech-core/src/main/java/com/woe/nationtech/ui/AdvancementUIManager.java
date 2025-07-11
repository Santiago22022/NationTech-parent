package com.woe.nationtech.ui;

import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.RootAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.woe.nationtech.NationTech;
import com.woe.nationtech.data.NationData;
import com.woe.nationtech.data.NationDataManager;
import com.woe.nationtech.data.Technology;
import com.woe.nationtech.data.TechnologyManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class AdvancementUIManager {

    private final NationTech plugin;
    private final TechnologyManager technologyManager;
    private final NationDataManager nationDataManager;
    private final AdvancementTab techTab;
    private final Map<String, BaseAdvancement> advancements = new HashMap<>();

    public AdvancementUIManager(NationTech plugin) {
        this.plugin = plugin;
        this.technologyManager = plugin.getTechnologyManager();
        this.nationDataManager = plugin.getNationDataManager();
        UltimateAdvancementAPI api = UltimateAdvancementAPI.getInstance(plugin);

        this.techTab = api.createAdvancementTab("nationtech");
        String rootTitle = LegacyComponentSerializer.legacyAmpersand().serialize(Component.text("Tecnologías de la Nación"));
        String rootDescription = LegacyComponentSerializer.legacyAmpersand().serialize(Component.text("El árbol de progreso de tu nación."));
        AdvancementDisplay rootDisplay = new AdvancementDisplay(Material.KNOWLEDGE_BOOK, rootTitle, AdvancementFrameType.TASK, false, false, 0, 0, rootDescription);

        RootAdvancement root = new RootAdvancement(techTab, "root", rootDisplay, "textures/gui/advancements/backgrounds/stone.png");

        Map<String, Advancement> createdAdvancementsForParenting = new HashMap<>();
        createdAdvancementsForParenting.put("root", root);

        List<Technology> allTechs = new ArrayList<>(technologyManager.getTechnologies());
        int lastSize;
        do {
            lastSize = allTechs.size();
            Iterator<Technology> iterator = allTechs.iterator();
            while (iterator.hasNext()) {
                Technology tech = iterator.next();
                String parentId = tech.dependencies().isEmpty() ? "root" : tech.dependencies().get(0);
                if (createdAdvancementsForParenting.containsKey(parentId)) {
                    Advancement parentAdv = createdAdvancementsForParenting.get(parentId);
                    // Al crear el BaseAdvancement con su padre, se enlaza automáticamente al árbol.
                    BaseAdvancement newAdv = createAdvancementForTech(tech, parentAdv);

                    createdAdvancementsForParenting.put(tech.id(), newAdv);
                    this.advancements.put(tech.id(), newAdv);
                    iterator.remove();
                }
            }
        } while (allTechs.size() < lastSize && !allTechs.isEmpty());

        // Ya no es necesario registrar los logros hijos manualmente.
        // La API los descubre a través de la jerarquía de padres.
    }

    private BaseAdvancement createAdvancementForTech(Technology tech, Advancement parent) {
        List<String> description = new ArrayList<>();
        tech.description().forEach(line -> description.add(LegacyComponentSerializer.legacyAmpersand().serialize(Component.text(line))));
        description.add("");
        String costLine = LegacyComponentSerializer.legacyAmpersand().serialize(Component.text("Coste: " + tech.cost() + " Niveles de XP").color(NamedTextColor.GOLD));
        description.add(costLine);

        String displayName = LegacyComponentSerializer.legacyAmpersand().serialize(Component.text(tech.displayName()));

        AdvancementDisplay display = new AdvancementDisplay(tech.icon(), displayName, AdvancementFrameType.GOAL, true, true, tech.x(), tech.y(), description);

        return new BaseAdvancement(tech.id(), display, parent);
    }

    public void openTechTree(Player player) {
        techTab.showTab(player);
    }

    public void updateAdvancementsFor(Player player) {
        Resident resident = TownyAPI.getInstance().getResident(player);
        Nation nation = null;
        if (resident != null && resident.hasTown()) {
            try {
                nation = resident.getTown().getNation();
            } catch (NotRegisteredException ignored) {}
        }

        if (nation == null) {
            for (BaseAdvancement advancement : advancements.values()) {
                advancement.revoke(player);
            }
            return;
        }

        NationData nationData = nationDataManager.getNationData(nation.getUUID());

        for (Map.Entry<String, BaseAdvancement> entry : advancements.entrySet()) {
            String techId = entry.getKey();
            BaseAdvancement advancement = entry.getValue();
            if (nationData.hasTechnology(techId)) {
                advancement.grant(player);
            } else {
                advancement.revoke(player);
            }
        }
    }

    public void updateAdvancementsForNation(Nation nation) {
        List<Player> members = TownyAPI.getInstance().getOnlinePlayers(nation);
        for (Player member : members) {
            updateAdvancementsFor(member);
        }
    }
}