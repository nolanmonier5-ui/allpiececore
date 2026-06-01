package dev.serverforge.util;

import dev.serverforge.ServerForgePlugin;
import dev.serverforge.craft.gui.CraftMainGui;
import dev.serverforge.shop.gui.ShopMainGui;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/** Toutes les commandes du plugin. */
public class Commands implements CommandExecutor {
    private final ServerForgePlugin plugin;
    public Commands(ServerForgePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        String name = command.getName().toLowerCase();

        if (name.equals("shopadmin") || name.equals("craftadmin")) {
            if (!sender.hasPermission("serverforge.admin")) { sender.sendMessage(Text.color("&cPermission manquante.")); return true; }
            if (args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
                plugin.reloadConfig(); plugin.shop().loadAll(); plugin.craft().loadAll(); plugin.chests().loadAll();
                sender.sendMessage(Text.color("&aServerForge recharge."));
                return true;
            }
            if (!(sender instanceof Player player)) { sender.sendMessage("Joueurs uniquement."); return true; }
            if (name.equals("shopadmin")) new ShopMainGui(plugin, player, true).open(player);
            else new CraftMainGui(plugin, player, true).open(player);
            return true;
        }

        if (name.equals("lootchest")) {
            if (!sender.hasPermission("serverforge.admin")) { sender.sendMessage(Text.color("&cPermission manquante.")); return true; }
            if (!(sender instanceof Player player)) { sender.sendMessage("Joueurs uniquement."); return true; }
            ItemUtil.give(player, plugin.chestTool().create());
            player.sendMessage(Text.color("&aOutil coffre a butin recu."));
            return true;
        }

        if (!(sender instanceof Player player)) { sender.sendMessage("Joueurs uniquement."); return true; }
        if (name.equals("shop")) new ShopMainGui(plugin, player, false).open(player);
        else if (name.equals("craft")) new CraftMainGui(plugin, player, false).open(player);
        return true;
    }
}
