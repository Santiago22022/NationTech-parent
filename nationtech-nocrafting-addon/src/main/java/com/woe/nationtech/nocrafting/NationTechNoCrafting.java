package com.woe.nationtech.nocrafting;

import com.woe.nationtech.nocrafting.listeners.CraftingListener;
import com.woe.nationtech.nocrafting.listeners.RecipeListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class NationTechNoCrafting extends JavaPlugin {

    @Override
    public void onEnable() {
        if (getServer().getPluginManager().getPlugin("NationTech") == null) {
            getLogger().severe("NationTech-Core not found! This addon will be disabled.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(new CraftingListener(), this);
        getServer().getPluginManager().registerEvents(new RecipeListener(), this);

        getLogger().info("NationTech-NoCrafting addon has been enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("NationTech-NoCrafting addon has been disabled.");
    }
}