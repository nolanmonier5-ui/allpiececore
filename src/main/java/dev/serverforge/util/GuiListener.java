package dev.serverforge.util;

import dev.serverforge.ServerForgePlugin;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.InventoryHolder;

/** Route les clics de GUI et capte la saisie chat. Autorise le depot d'items dans les GUI qui le permettent (craft). */
public class GuiListener implements Listener {
    private final ServerForgePlugin plugin;
    public GuiListener(ServerForgePlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof Gui gui)) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        int raw = event.getRawSlot();
        boolean topInv = raw < event.getInventory().getSize();

        // GUI craft : autoriser le depot/retrait d'items dans certains slots
        if (topInv && gui.allowsRawInteraction(event.getSlot())) {
            // on laisse l'action vanilla (placer/retirer) se faire
            return;
        }
        // Shift-click depuis l'inventaire du joueur vers une GUI a slots libres : autoriser
        if (!topInv && event.isShiftClick() && hasFreeRawSlots(gui)) {
            return;
        }

        event.setCancelled(true);
        if (event.getClickedInventory() == null) return;
        if (!event.getInventory().equals(event.getClickedInventory())) return;
        gui.click(event.getSlot(), event.getClick(), player);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof Gui gui)) return;
        // Autorise le drag uniquement si tous les slots concernes acceptent l'interaction brute
        for (int raw : event.getRawSlots()) {
            if (raw < event.getInventory().getSize() && !gui.allowsRawInteraction(raw)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    private boolean hasFreeRawSlots(Gui gui) {
        // heuristique : si la GUI autorise au moins un slot brut, on permet le shift-click
        for (int i = 0; i < gui.getInventory().getSize(); i++) if (gui.allowsRawInteraction(i)) return true;
        return false;
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof Gui gui)) return;
        if (!(event.getPlayer() instanceof Player player)) return;
        // Si la GUI a des slots a interaction brute (craft) : enregistre la recette
        // puis rend au joueur les items qu'il avait deposes (sinon ils seraient perdus).
        boolean hasRaw = false;
        for (int i = 0; i < event.getInventory().getSize(); i++)
            if (gui.allowsRawInteraction(i)) { hasRaw = true; break; }
        if (!hasRaw) return;

        if (gui instanceof dev.serverforge.craft.gui.RecipeEditGui re) {
            re.sync();
            for (int i = 0; i < event.getInventory().getSize(); i++) {
                if (!gui.allowsRawInteraction(i)) continue;
                if (re.isGhost(i)) { event.getInventory().setItem(i, null); continue; } // pre-rempli : ne pas restituer
                var it = event.getInventory().getItem(i);
                if (it != null && it.getType() != org.bukkit.Material.AIR) {
                    ItemUtil.give(player, it);
                    event.getInventory().setItem(i, null);
                }
            }
            return;
        }
        for (int i = 0; i < event.getInventory().getSize(); i++) {
            if (!gui.allowsRawInteraction(i)) continue;
            var it = event.getInventory().getItem(i);
            if (it != null && it.getType() != org.bukkit.Material.AIR) {
                ItemUtil.give(player, it);
                event.getInventory().setItem(i, null);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onChat(AsyncChatEvent event) {
        Player p = event.getPlayer();
        if (!plugin.chat().isWaiting(p.getUniqueId())) return;
        String msg = PlainTextComponentSerializer.plainText().serialize(event.message());
        event.setCancelled(true);
        plugin.chat().consume(p, msg);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) { plugin.chat().clear(event.getPlayer().getUniqueId()); }
}
