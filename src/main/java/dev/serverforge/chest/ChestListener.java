package dev.serverforge.chest;

import dev.serverforge.ServerForgePlugin;
import dev.serverforge.chest.gui.LootChestGui;
import dev.serverforge.chest.model.LootChest;
import dev.serverforge.chest.model.LootEntry;
import dev.serverforge.util.ItemUtil;
import dev.serverforge.util.Text;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Outil coffres :
 * - clic DROIT sur un coffre        : ouvre la config (et marque le coffre comme coffre a butin)
 * - clic GAUCHE sur un coffre       : COPIE la config de ce coffre dans le presse-papier
 * - SHIFT + clic GAUCHE sur un coffre : COLLE le presse-papier sur ce coffre
 * Ouverture normale d'un coffre a butin -> remplissage si cooldown ecoule.
 * A la fermeture, les items "uniques" lootes sont retires de TOUS les coffres.
 */
public class ChestListener implements Listener {
    private final ServerForgePlugin plugin;

    public ChestListener(ServerForgePlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null) return;
        if (!(block.getState() instanceof Chest)) return;
        Player p = event.getPlayer();
        boolean holdingTool = plugin.chestTool().isTool(event.getItem());

        if (holdingTool) {
            if (!p.hasPermission("serverforge.admin")) return;
            event.setCancelled(true); // empeche l'ouverture / la casse pendant la manipulation

            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                LootChest chest = plugin.chests().createOrGet(block.getLocation());
                plugin.chests().saveAll();
                plugin.setLastEditedChest(chest.key());
                new LootChestGui(plugin, block.getLocation()).open(p);

            } else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                if (p.isSneaking()) {
                    // COLLER
                    if (!plugin.hasClipboard()) { p.sendMessage(Text.color("&cPresse-papier vide. Fais d'abord clic gauche sur un coffre configure.")); return; }
                    LootChest target = plugin.chests().createOrGet(block.getLocation());
                    target.applyConfig(plugin.clipboardLoot(), plugin.clipboardReset());
                    plugin.chests().saveAll();
                    p.sendMessage(Text.color("&aConfig collee sur ce coffre (" + plugin.clipboardSize() + " items)."));
                } else {
                    // COPIER
                    LootChest source = plugin.chests().get(block.getLocation());
                    if (source == null) { p.sendMessage(Text.color("&cCe coffre n'est pas un coffre a butin. Configure-le d'abord (clic droit).")); return; }
                    plugin.copyToClipboard(source.getLoot(), source.getResetSeconds());
                    p.sendMessage(Text.color("&aConfig copiee (" + source.getLoot().size() + " items). Shift + clic gauche sur un autre coffre pour coller."));
                }
            }
            return;
        }

        // Ouverture normale d'un coffre a butin -> remplissage si necessaire
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && plugin.chests().isLootChest(block.getLocation())) {
            plugin.chests().tryFill(block, false);
        }
    }

    /** A la fermeture d'un coffre a butin : detecte les uniques lootes et les retire partout. */
    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Inventory inv = event.getInventory();
        InventoryHolder holder = inv.getHolder();
        if (!(holder instanceof Chest chest)) return;
        Location loc = chest.getBlock().getLocation();
        LootChest cfg = plugin.chests().get(loc);
        if (cfg == null || cfg.pendingOneTime().isEmpty()) return;

        List<LootEntry> stillPending = new ArrayList<>();
        for (LootEntry e : cfg.pendingOneTime()) {
            if (containsSimilar(inv, e.getItem())) {
                stillPending.add(e); // toujours dans le coffre = pas encore loote
            } else {
                plugin.chests().consumeOneTime(e.getItem()); // loote -> retire de tous les coffres
            }
        }
        cfg.pendingOneTime().clear();
        cfg.pendingOneTime().addAll(stillPending);
    }

    private boolean containsSimilar(Inventory inv, ItemStack like) {
        int cmd = ItemUtil.readModelData(like);
        for (ItemStack s : inv.getContents()) {
            if (s == null) continue;
            if (s.getType() == like.getType() && ItemUtil.readModelData(s) == cmd) return true;
        }
        return false;
    }
}
