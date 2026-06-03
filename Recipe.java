package dev.serverforge.chest.model;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

/** Un item droppable d'un coffre, avec sa chance (0-100%). */
public class LootEntry {
    private final ItemStack item;
    private double chance;

    public LootEntry(ItemStack item, double chance) {
        this.item = item;
        this.chance = clamp(chance);
    }
    public ItemStack getItem() { return item; }
    public double getChance() { return chance; }
    public void setChance(double c) { this.chance = clamp(c); }
    private static double clamp(double c) { return Math.max(0, Math.min(100, c)); }

    public void save(ConfigurationSection sec) { sec.set("item", item); sec.set("chance", chance); }
    public static LootEntry load(ConfigurationSection sec) {
        if (sec == null) return null;
        ItemStack it = sec.getItemStack("item");
        if (it == null) return null;
        return new LootEntry(it, sec.getDouble("chance", 100));
    }
}
