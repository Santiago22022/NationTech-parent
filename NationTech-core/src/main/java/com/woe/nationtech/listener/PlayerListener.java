package com.woe.nationtech.listener;

import com.fren_gor.ultimateAdvancementAPI.events.PlayerLoadingCompletedEvent;
import com.woe.nationtech.api.NationTechAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerListener implements Listener {

    private final JavaPlugin plugin;
    private final NationTechAPI api;

    public PlayerListener(JavaPlugin plugin, NationTechAPI api) {
        this.plugin = plugin;
        this.api = api;
    }

    @EventHandler
    public void onPlayerLoad(PlayerLoadingCompletedEvent event) {
        Player player = event.getPlayer();
        // Delay by 1 tick to ensure the API is fully initialized
        Bukkit.getScheduler().runTask(plugin, () -> {
            api.getAdvancementUIManager().updateAdvancementsFor(player);
            api.getAdvancementUIManager().openTechTree(player);
        });
    }
}
