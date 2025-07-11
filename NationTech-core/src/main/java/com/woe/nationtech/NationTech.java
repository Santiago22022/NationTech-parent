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

    private static NationTech instance;
    private TechnologyManager technologyManager;
    private NationDataManager nationDataManager;
    private AdvancementUIManager advancementUIManager;

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this));
    }

    @Override
    public void onEnable() {
        instance = this;
        CommandAPI.onEnable();

        saveDefaultConfig();

        this.technologyManager = new TechnologyManager(this);
        this.nationDataManager = new NationDataManager(this);
        // Creamos el UIManager después de los otros managers.
        this.advancementUIManager = new AdvancementUIManager(this);

        new CommandManager(this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        NationTechAPI.setManagers(technologyManager, nationDataManager, advancementUIManager);

        getLogger().info("NationTech has been enabled.");
    }

    @Override
    public void onDisable() {
        CommandAPI.onDisable();

        if (nationDataManager!= null) {
            nationDataManager.saveAllDirtyData();
        }
        getLogger().info("NationTech has been disabled.");
    }

    // Método para recargar el UIManager, llamado desde el comando de admin.
    public void reloadAdvancementManager() {
        this.advancementUIManager = new AdvancementUIManager(this);
        NationTechAPI.setManagers(technologyManager, nationDataManager, advancementUIManager);
    }

    public static NationTech getInstance() {
        return instance;
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