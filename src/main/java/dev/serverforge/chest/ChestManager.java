package dev.serverforge.chest;

import dev.serverforge.ServerForgePlugin;
import dev.serverforge.chest.model.LootChest;
import dev.serverforge.chest.model.LootEntry;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/** Gere les coffres a butin, leur persistance (chests.yml) et leur remplissage. */
public class ChestManager {
    private final ServerForgePlugin plugin;
    private final File file;
    private final Map<String, LootChest> chests = new HashMap<>();

    public ChestManager(ServerForgePlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "chests.yml");
    }

    public long defaultResetSeconds() { return plugin.getConfig().getLong("chest.default-reset-seconds", 86400); }

    public void loadAll() {
        chests.clear();
        if (file.exists()) {
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection root = cfg.getConfigurationSection("chests");
            if (root != null) for (String k : root.getKeys(false)) {
                LootChest c = LootChest.load(root.getConfigurationSection(k));
                if (c != null) chests.put(c.key(), c);
            }
        }
        plugin.getLogger().info(chests.size() + " coffre(s) a butin charge(s).");
    }

    public void saveAll() {
        YamlConfiguration cfg = new YamlConfiguration();
        ConfigurationSection root = cfg.createSection("chests");
        int i = 0;
        for (LootChest c : chests.values()) c.save(root.createSection("c" + (i++)));
        try { cfg.save(file); }
        catch (IOException e) { plugin.getLogger().severe("Echec sauvegarde chests.yml: " + e.getMessage()); }
    }

    public LootChest get(Location loc) { return chests.get(LootChest.key(loc)); }
    public LootChest allByKey(String key) { return chests.get(key); }
    public boolean isLootChest(Location loc) { return chests.containsKey(LootChest.key(loc)); }

    public LootChest createOrGet(Location loc) {
        String k = LootChest.key(loc);
        return chests.computeIfAbsent(k, key -> new LootChest(loc, defaultResetSeconds()));
    }

    public void remove(Location loc) { chests.remove(LootChest.key(loc)); saveAll(); }

    /** Remplit le coffre selon le loot/chances si le cooldown est ecoule (ou force). */
    public void tryFill(Block block, boolean force) {
        if (!(block.getState() instanceof Chest chest)) return;
        LootChest cfg = get(block.getLocation());
        if (cfg == null) return;
        long now = System.currentTimeMillis();
        if (!force && cfg.getResetSeconds() > 0
                && now - cfg.getLastFilled() < cfg.getResetSeconds() * 1000L) {
            return; // pas encore l'heure
        }
        Inventory inv = chest.getInventory();
        inv.clear();
        cfg.pendingOneTime().clear();
        for (LootEntry e : cfg.getLoot()) {
            if (ThreadLocalRandom.current().nextDouble(100) < e.getChance()) {
                inv.addItem(e.getItem().clone());
                if (e.isOneTime()) cfg.pendingOneTime().add(e); // a surveiller a la fermeture
            }
        }
        cfg.setLastFilled(now);
        saveAll();
    }

    /**
     * Retire de TOUS les coffres les loots equivalents (meme type + CMD) a l'item donne.
     * Appele quand un item "unique" a ete loote : il ne reapparaitra plus nulle part.
     */
    public void consumeOneTime(ItemStack proto) {
        if (proto == null) return;
        int cmd = dev.serverforge.util.ItemUtil.readModelData(proto);
        int removed = 0;
        for (LootChest c : chests.values()) {
            var it = c.getLoot().iterator();
            while (it.hasNext()) {
                LootEntry e = it.next();
                if (e.isOneTime() && e.getItem().getType() == proto.getType()
                        && dev.serverforge.util.ItemUtil.readModelData(e.getItem()) == cmd) {
                    it.remove(); removed++;
                }
            }
            c.pendingOneTime().removeIf(e -> e.getItem().getType() == proto.getType()
                    && dev.serverforge.util.ItemUtil.readModelData(e.getItem()) == cmd);
        }
        if (removed > 0) saveAll();
    }
}
