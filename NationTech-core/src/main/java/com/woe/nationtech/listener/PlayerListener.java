package com.woe.nationtech.listeners;

import com.frengor.ultimateadvancementapi.events.PlayerLoadingCompletedEvent;
import com.woe.nationtech.NationTech;
import com.woe.nationtech.ui.AdvancementUIManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerListener implements Listener {

    private final AdvancementUIManager advancementUIManager;

    public PlayerListener(NationTech plugin) {
        this.advancementUIManager = plugin.getAdvancementUIManager();
    }

    @EventHandler
    public void onPlayerLoad(PlayerLoadingCompletedEvent event) {
        Player player = event.getPlayer();
        advancementUIManager.showTechTree(player);
        advancementUIManager.updateAdvancementsFor(player);
    }
}