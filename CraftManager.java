package dev.serverforge.chest;

import dev.serverforge.ServerForgePlugin;
import dev.serverforge.chest.gui.LootChestGui;
import dev.serverforge.chest.model.LootChest;
import dev.serverforge.util.Text;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Outil coffres :
 * - clic DROIT sur un coffre : ouvre la config (et marque le coffre comme coffre a butin)
 * - clic GAUCHE sur un coffre : copie la config du dernier coffre configure sur celui-ci
 * Sinon, ouverture normale d'un coffre a butin -> remplissage si cooldown ecoule.
 */
public class ChestListener implements Listener {
    private final ServerForgePlugin plugin;

    public ChestListener(ServerForgePlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null) return;
        if (!(block.getState() instanceof Chest)) {
            // Outil clique dans le vide : rien
            return;
        }
        Player p = event.getPlayer();
        boolean holdingTool = plugin.chestTool().isTool(event.getItem());

        if (holdingTool) {
            if (!p.hasPermission("serverforge.admin")) return;
            event.setCancelled(true); // empeche l'ouverture vanilla pendant la config

            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                LootChest chest = plugin.chests().createOrGet(block.getLocation());
                plugin.chests().saveAll();
                plugin.setLastEditedChest(chest.key());
                new LootChestGui(plugin, block.getLocation()).open(p);
            } else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                String last = plugin.getLastEditedChest();
                if (last == null) { p.sendMessage(Text.color("&cConfigure d'abord un coffre (clic droit).")); return; }
                LootChest source = plugin.chests().allByKey(last);
                if (source == null) { p.sendMessage(Text.color("&cConfig source introuvable.")); return; }
                LootChest target = plugin.chests().createOrGet(block.getLocation());
                target.copyFrom(source);
                plugin.chests().saveAll();
                p.sendMessage(Text.color("&aConfig copiee sur ce coffre (" + source.getLoot().size() + " items)."));
            }
            return;
        }

        // Ouverture normale d'un coffre a butin -> remplissage si necessaire
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && plugin.chests().isLootChest(block.getLocation())) {
            plugin.chests().tryFill(block, false);
        }
    }
}
