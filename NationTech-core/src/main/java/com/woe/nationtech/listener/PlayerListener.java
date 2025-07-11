package com.woe.nationtech.listener;

import com.fren_gor.ultimateAdvancementAPI.events.PlayerLoadingCompletedEvent;
import com.woe.nationtech.NationTech;
import com.woe.nationtech.ui.AdvancementUIManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerListener implements Listener {

    private final NationTech plugin;
    private final AdvancementUIManager advancementUIManager;

    public PlayerListener(NationTech plugin) {
        this.plugin = plugin;
        this.advancementUIManager = plugin.getAdvancementUIManager();
    }

    @EventHandler
    public void onPlayerLoad(PlayerLoadingCompletedEvent event) {
        Player player = event.getPlayer();
        // CORRECCIÓN: Retrasar la ejecución 1 tick para darle tiempo a la API a inicializarse.
        Bukkit.getScheduler().runTask(plugin, () -> {
            advancementUIManager.updateAdvancementsFor(player);
            advancementUIManager.openTechTree(player);
        });
    }
}
