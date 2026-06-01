package dev.serverforge.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/** Base de toutes les interfaces (InventoryHolder pour router les clics sans ambiguite). */
public abstract class Gui implements InventoryHolder {

    @FunctionalInterface
    public interface ClickAction { void run(Player player, ClickType click); }

    protected final Inventory inventory;
    private final Map<Integer, ClickAction> actions = new HashMap<>();

    protected Gui(String title, int rows) {
        this.inventory = Bukkit.createInventory(this, rows * 9, Text.color(title));
    }

    @NotNull @Override public Inventory getInventory() { return inventory; }

    protected void setButton(int slot, ItemStack item, ClickAction action) {
        if (slot < 0 || slot >= inventory.getSize()) return;
        inventory.setItem(slot, item);
        if (action != null) actions.put(slot, action); else actions.remove(slot);
    }
    protected void clear() { inventory.clear(); actions.clear(); }

    /** Renvoie true si le clic etait gere par un bouton. */
    public boolean click(int slot, ClickType type, Player player) {
        ClickAction a = actions.get(slot);
        if (a == null) return false;
        a.run(player, type);
        return true;
    }
    /** Certaines GUI (craft) autorisent le depot d'items dans des slots libres. */
    public boolean allowsRawInteraction(int slot) { return false; }

    public void open(Player player) { player.openInventory(inventory); }
    public abstract void build();
}
