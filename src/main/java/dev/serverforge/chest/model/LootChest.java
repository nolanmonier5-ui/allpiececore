package dev.serverforge.chest.model;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration d'un coffre a butin lie a une position.
 * - loot         : items droppables avec leur %
 * - resetSeconds : delai de re-remplissage
 * - lastFilled   : timestamp du dernier remplissage (pour le cooldown)
 */
public class LootChest {
    private final String world;
    private final int x, y, z;
    private final List<LootEntry> loot = new ArrayList<>();
    private long resetSeconds;
    private long lastFilled = 0;

    public LootChest(Location loc, long resetSeconds) {
        this.world = loc.getWorld().getName();
        this.x = loc.getBlockX(); this.y = loc.getBlockY(); this.z = loc.getBlockZ();
        this.resetSeconds = resetSeconds;
    }
    private LootChest(String world, int x, int y, int z, long resetSeconds) {
        this.world = world; this.x = x; this.y = y; this.z = z; this.resetSeconds = resetSeconds;
    }

    public static String key(Location loc) {
        return loc.getWorld().getName() + ";" + loc.getBlockX() + ";" + loc.getBlockY() + ";" + loc.getBlockZ();
    }
    public String key() { return world + ";" + x + ";" + y + ";" + z; }

    public List<LootEntry> getLoot() { return loot; }
    public long getResetSeconds() { return resetSeconds; }
    public void setResetSeconds(long s) { this.resetSeconds = Math.max(0, s); }
    public long getLastFilled() { return lastFilled; }
    public void setLastFilled(long t) { this.lastFilled = t; }
    public String getWorld() { return world; }
    public int getX() { return x; } public int getY() { return y; } public int getZ() { return z; }

    /** Copie le loot + le delai d'un autre coffre (pour la fonction "copier la config"). */
    public void copyFrom(LootChest other) {
        loot.clear();
        for (LootEntry e : other.loot) loot.add(new LootEntry(e.getItem().clone(), e.getChance()));
        this.resetSeconds = other.resetSeconds;
        this.lastFilled = 0;
    }

    public void save(ConfigurationSection sec) {
        sec.set("world", world); sec.set("x", x); sec.set("y", y); sec.set("z", z);
        sec.set("reset-seconds", resetSeconds);
        sec.set("last-filled", lastFilled);
        ConfigurationSection ls = sec.createSection("loot");
        int i = 0;
        for (LootEntry e : loot) e.save(ls.createSection("e" + (i++)));
    }

    public static LootChest load(ConfigurationSection sec) {
        if (sec == null) return null;
        LootChest c = new LootChest(sec.getString("world", "world"),
                sec.getInt("x"), sec.getInt("y"), sec.getInt("z"),
                sec.getLong("reset-seconds", 86400));
        c.lastFilled = sec.getLong("last-filled", 0);
        ConfigurationSection ls = sec.getConfigurationSection("loot");
        if (ls != null) for (String k : ls.getKeys(false)) {
            LootEntry e = LootEntry.load(ls.getConfigurationSection(k));
            if (e != null) c.loot.add(e);
        }
        return c;
    }
}
