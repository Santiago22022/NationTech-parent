package com.woe.nationtech.cmds;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.woe.nationtech.NationTech;
import com.woe.nationtech.data.Technology;
import com.woe.nationtech.data.TechnologyManager;
import com.woe.nationtech.ui.AdvancementUIManager;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class CommandManager {

    private final NationTech plugin;
    private final TechnologyManager technologyManager;
    private final AdvancementUIManager advancementUIManager;

    public CommandManager(NationTech plugin) {
        this.plugin = plugin;
        this.technologyManager = plugin.getTechnologyManager();
        this.advancementUIManager = plugin.getAdvancementUIManager();
        registerCommands();
    }

    private void registerCommands() {
        // Comando principal con subcomandos
        new CommandAPICommand("nationtech")
                .withPermission("nationtech.command.view")
                .withSubcommand(new CommandAPICommand("view")
                        .executesPlayer((player, args) -> {
                            advancementUIManager.openTechTree(player);
                        })
                )
                .withSubcommand(new CommandAPICommand("unlock")
                        .withPermission("nationtech.command.unlock")
                        .withArguments(new StringArgument("tech_id").replaceSuggestions(ArgumentSuggestions.strings(info ->
                                technologyManager.getTechnologies().stream()
                                        .map(Technology::id)
                                        .toArray(String[]::new)
                        )))
                        .executesPlayer((player, args) -> {
                            unlockTechnology(player, (String) args.get("tech_id"));
                        })
                )
                .withSubcommand(new CommandAPICommand("admin")
                        .withPermission("nationtech.admin")
                        .withSubcommand(new CommandAPICommand("reload")
                                .executes((sender, args) -> {
                                    plugin.reloadConfig();
                                    technologyManager.loadTechnologies();
                                    sender.sendMessage(ChatColor.GREEN + "NationTech configuration reloaded.");
                                })
                        )
                )
                .register();

        new CommandAPICommand("techtree")
                .withAliases("tecnologias", "tech") // Alias para el comando
                .withPermission("nationtech.command.view") // Usamos el mismo permiso
                .executesPlayer((player, args) -> {
                    advancementUIManager.openTechTree(player);
                })
                .register();
    }

    private void unlockTechnology(Player player, String techId) {
        Resident resident = TownyAPI.getInstance().getResident(player);
        if (resident == null ||!resident.hasTown()) {
            player.sendMessage(ChatColor.RED + "You must be in a town to do that.");
            return;
        }

        try {
            Nation nation = resident.getTown().getNation();
            technologyManager.unlockTechnology(player, nation, techId);
        } catch (NotRegisteredException e) {
            player.sendMessage(ChatColor.RED + "Your town is not part of a nation.");
        }
    }
}