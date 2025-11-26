package com.woe.nationtech.data;

import com.palmergames.bukkit.towny.object.Nation; // Import específico
import com.woe.nationtech.NationTech;
import com.woe.nationtech.ui.AdvancementUIManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class TechnologyManager {

    private final NationTech plugin;
    private final NationDataManager dataManager;
    private AdvancementUIManager uiManager;
    private final Set<Technology> technologies = new HashSet<>();

    public TechnologyManager(NationTech plugin, NationDataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        loadTechnologies();
    }

    public void setUiManager(AdvancementUIManager uiManager) {
        this.uiManager = uiManager;
    }

    public void loadTechnologies() {
        technologies.clear();
        File technologiesFile = new File(plugin.getDataFolder(), "technologies.yml");
        if (!technologiesFile.exists()) {
            plugin.saveResource("technologies.yml", false);
        }
        FileConfiguration technologiesConfig = YamlConfiguration.loadConfiguration(technologiesFile);
        ConfigurationSection techSection = technologiesConfig.getConfigurationSection("technologies");
        if (techSection == null) {
            plugin.getLogger().severe("'technologies' section not found in technologies.yml!");
            return;
        }

        for (String key : techSection.getKeys(false)) {
            ConfigurationSection section = techSection.getConfigurationSection(key);
            if (section != null) {
                try {
                    Technology tech = new Technology(
                            key,
                            ChatColor.translateAlternateColorCodes('&', section.getString("display-name", "Unnamed Tech")),
                            Material.valueOf(section.getString("icon", "BARRIER").toUpperCase()),
                            section.getStringList("description").stream().map(s -> ChatColor.translateAlternateColorCodes('&', s)).collect(Collectors.toList()),
                            section.getStringList("dependencies"),
                            section.getInt("cost", 0),
                            section.getInt("x-coord"),
                            section.getInt("y-coord"),
                            section.getStringList("recipe-keys")
                    );
                    technologies.add(tech);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().log(Level.WARNING, "Invalid material icon for technology: " + key, e);
                }
            }
        }
        plugin.getLogger().info("Loaded " + technologies.size() + " technologies.");
    }

    public Technology getTechnology(String id) {
        return technologies.stream().filter(t -> t.id().equalsIgnoreCase(id)).findFirst().orElse(null);
    }

    public Set<Technology> getTechnologies() {
        return new HashSet<>(technologies);
    }

    public void unlockTechnology(Player player, Nation nation, String techId) {
        NationData nationData = dataManager.getNationData(nation.getUUID());
        if (nationData.hasTechnology(techId)) {
            player.sendMessage(ChatColor.RED + "Your nation has already unlocked this technology.");
            return;
        }

        Technology tech = getTechnology(techId);
        if (tech == null) {
            player.sendMessage(ChatColor.RED + "Technology not found.");
            return;
        }

        for (String depId : tech.dependencies()) {
            if (!nationData.hasTechnology(depId)) {
                player.sendMessage(ChatColor.RED + "Your nation is missing the prerequisite technology: " + getTechnology(depId).displayName());
                return;
            }
        }

        if (player.getLevel() < tech.cost()) {
            player.sendMessage(ChatColor.RED + "You need " + tech.cost() + " experience levels to unlock this.");
            return;
        }

        player.setLevel(player.getLevel() - tech.cost());
        nationData.unlockTechnology(techId);
        player.sendMessage(ChatColor.GREEN + "You have unlocked: " + tech.displayName());

        uiManager.updateAdvancementsForNation(nation);
    }
}