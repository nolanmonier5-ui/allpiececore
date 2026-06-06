package dev.serverforge.chest.model;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

/** Un item droppable d'un coffre, avec sa chance (0-100%) et un flag "unique". */
public class LootEntry {
    private final ItemStack item;
    private double chance;
    private boolean oneTime = false; // si true : une fois loote, retire de TOUS les coffres

    public LootEntry(ItemStack item, double chance) {
        this.item = item;
        this.chance = clamp(chance);
    }
    public LootEntry(ItemStack item, double chance, boolean oneTime) {
        this.item = item;
        this.chance = clamp(chance);
        this.oneTime = oneTime;
    }
    public ItemStack getItem() { return item; }
    public double getChance() { return chance; }
    public void setChance(double c) { this.chance = clamp(c); }
    public boolean isOneTime() { return oneTime; }
    public void setOneTime(boolean o) { this.oneTime = o; }
    private static double clamp(double c) { return Math.max(0, Math.min(100, c)); }

    public void save(ConfigurationSection sec) {
        sec.set("item", item); sec.set("chance", chance); sec.set("one-time", oneTime);
    }
    public static LootEntry load(ConfigurationSection sec) {
        if (sec == null) return null;
        ItemStack it = sec.getItemStack("item");
        if (it == null) return null;
        return new LootEntry(it, sec.getDouble("chance", 100), sec.getBoolean("one-time", false));
    }
}
