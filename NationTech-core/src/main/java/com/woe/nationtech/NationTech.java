package com.woe.nationtech;

import com.woe.nationtech.api.NationTechAPI;
import com.woe.nationtech.cmds.CommandManager;
import com.woe.nationtech.data.NationDataManager;
import com.woe.nationtech.data.TechnologyManager;
import com.woe.nationtech.listener.PlayerListener;
import com.woe.nationtech.ui.AdvancementUIManager;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import org.bukkit.plugin.java.JavaPlugin;

public final class NationTech extends JavaPlugin {

    private NationDataManager nationDataManager;

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this));
    }

    @Override
    public void onEnable() {
        CommandAPI.onEnable();
        saveDefaultConfig();

        // Initialize managers
        nationDataManager = new NationDataManager(this);
        TechnologyManager technologyManager = new TechnologyManager(this, nationDataManager);
        AdvancementUIManager advancementUIManager = new AdvancementUIManager(this, technologyManager, nationDataManager);
        technologyManager.setUiManager(advancementUIManager);

        // Initialize API
        NationTechAPI api = new NationTechAPI(technologyManager, nationDataManager, advancementUIManager);

        // Register commands and listeners
        new CommandManager(this, api);
        getServer().getPluginManager().registerEvents(new PlayerListener(this, api), this);

        getLogger().info("NationTech has been enabled.");
    }

    @Override
    public void onDisable() {
        CommandAPI.onDisable();
        if (nationDataManager != null) {
            nationDataManager.close();
        }
        getLogger().info("NationTech has been disabled.");
    }
}