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
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.stream.Collectors;

public class AdvancementUIManager {

    private final TechnologyManager technologyManager;
    private final NationDataManager nationDataManager;
    private final AdvancementTab techTab;
    private final Map<String, Advancement> advancements = new HashMap<>();

    public AdvancementUIManager(JavaPlugin plugin, TechnologyManager technologyManager, NationDataManager nationDataManager) {
        this.technologyManager = technologyManager;
        this.nationDataManager = nationDataManager;
        UltimateAdvancementAPI api = UltimateAdvancementAPI.getInstance(plugin);

        this.techTab = api.createAdvancementTab("nationtech");
        String rootTitle = LegacyComponentSerializer.legacyAmpersand().serialize(Component.text("Tecnologías de la Nación"));
        String rootDescription = LegacyComponentSerializer.legacyAmpersand().serialize(Component.text("El árbol de progreso de tu nación."));
        AdvancementDisplay rootDisplay = new AdvancementDisplay(Material.KNOWLEDGE_BOOK, rootTitle, AdvancementFrameType.TASK, false, false, 0, 0, rootDescription);

        RootAdvancement root = new RootAdvancement(techTab, "root", rootDisplay, "textures/gui/advancements/backgrounds/stone.png");
        advancements.put("root", root);

        // Build the advancement tree recursively
        buildAdvancementTree("root", root);
    }

    private void buildAdvancementTree(String parentId, Advancement parent) {
        technologyManager.getTechnologies().stream()
                .filter(tech -> tech.dependencies().contains(parentId))
                .forEach(tech -> {
                    BaseAdvancement advancement = createAdvancementForTech(tech, parent);
                    advancements.put(tech.id(), advancement);
                    buildAdvancementTree(tech.id(), advancement);
                });
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
            for (Advancement advancement : advancements.values()) {
                if (advancement instanceof BaseAdvancement) {
                    ((BaseAdvancement) advancement).revoke(player);
                }
            }
            return;
        }

        NationData nationData = nationDataManager.getNationData(nation.getUUID());

        for (Map.Entry<String, Advancement> entry : advancements.entrySet()) {
            String techId = entry.getKey();
            Advancement advancement = entry.getValue();
            if (advancement instanceof BaseAdvancement) {
                if (nationData.hasTechnology(techId)) {
                    ((BaseAdvancement) advancement).grant(player);
                } else {
                    ((BaseAdvancement) advancement).revoke(player);
                }
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