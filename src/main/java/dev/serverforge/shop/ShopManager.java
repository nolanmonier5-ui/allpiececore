package dev.serverforge.shop;

import dev.serverforge.ServerForgePlugin;
import dev.serverforge.shop.model.ShopCategory;
import dev.serverforge.shop.model.ShopItem;
import dev.serverforge.shop.model.ShopMenuEntry;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Gere les categories/items du shop, leur persistance et le reset periodique des prix. */
public class ShopManager {

    private final ServerForgePlugin plugin;
    private final File file;
    private final Map<String, ShopCategory> categories = new LinkedHashMap<>();
    private final Map<String, ShopMenuEntry> entries = new LinkedHashMap<>();

    public ShopManager(ServerForgePlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "shop.yml");
    }

    // ---- Config accessors ----
    public double defaultBuyStep() { return plugin.getConfig().getDouble("shop.default-buy-step", 2.0) / 100.0; }
    public double defaultSellStep() { return plugin.getConfig().getDouble("shop.default-sell-step", 2.0) / 100.0; }
    public double minMultiplier() { return plugin.getConfig().getDouble("shop.min-multiplier", 0.25); }
    public double maxMultiplier() { return plugin.getConfig().getDouble("shop.max-multiplier", 4.0); }
    public double sellRatio() { return plugin.getConfig().getDouble("shop.sell-ratio", 0.5); }
    public long resetMinutes() { return plugin.getConfig().getLong("shop.reset-minutes", 30); }
    public String mainTitle() { return plugin.getConfig().getString("shop.main-title", "&8Boutique"); }
    public void setMainTitle(String t) { plugin.getConfig().set("shop.main-title", t); plugin.saveConfig(); }

    // ---- Load / save ----
    public void loadAll() {
        categories.clear();
        entries.clear();
        if (file.exists()) {
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection root = cfg.getConfigurationSection("categories");
            if (root != null) {
                for (String id : root.getKeys(false)) {
                    ConfigurationSection s = root.getConfigurationSection(id);
                    if (s != null) categories.put(id, ShopCategory.load(id, s));
                }
            }
            ConfigurationSection er = cfg.getConfigurationSection("entries");
            if (er != null) {
                for (String id : er.getKeys(false)) {
                    ConfigurationSection s = er.getConfigurationSection(id);
                    if (s != null) entries.put(id, ShopMenuEntry.load(id, s));
                }
            }
        }
        if (categories.isEmpty()) {
            generateDefaults();
            saveAll();
        }
        plugin.getLogger().info(categories.size() + " categorie(s) de shop chargee(s).");
    }

    public void saveAll() {
        YamlConfiguration cfg = new YamlConfiguration();
        ConfigurationSection root = cfg.createSection("categories");
        for (ShopCategory c : categories.values()) c.save(root.createSection(c.getId()));
        ConfigurationSection er = cfg.createSection("entries");
        for (ShopMenuEntry e : entries.values()) e.save(er.createSection(e.getId()));
        try { cfg.save(file); }
        catch (IOException e) { plugin.getLogger().severe("Echec sauvegarde shop.yml: " + e.getMessage()); }
    }

    public ShopCategory create(String id, String name) {
        String safe = id.toLowerCase().replaceAll("[^a-z0-9_-]", "_");
        if (categories.containsKey(safe)) return null;
        ShopCategory c = new ShopCategory(safe, name);
        categories.put(safe, c);
        saveAll();
        return c;
    }
    public void delete(String id) {
        categories.remove(id);
        entries.values().removeIf(e -> e.getCategoryId().equals(id));
        saveAll();
    }
    public ShopCategory get(String id) { return categories.get(id); }
    public List<ShopCategory> all() { return new ArrayList<>(categories.values()); }

    // ---- Icones du menu principal (entrees) ----
    public List<ShopMenuEntry> entries() { return new ArrayList<>(entries.values()); }
    public List<ShopMenuEntry> entriesFor(String categoryId) {
        List<ShopMenuEntry> out = new ArrayList<>();
        for (ShopMenuEntry e : entries.values()) if (e.getCategoryId().equals(categoryId)) out.add(e);
        return out;
    }
    public ShopMenuEntry getEntry(String id) { return entries.get(id); }
    public ShopMenuEntry createEntry(String categoryId, String displayName, String iconMaterial, int iconModelData) {
        ShopMenuEntry e = ShopMenuEntry.create(categoryId, displayName);
        e.setIconMaterial(iconMaterial == null ? "CHEST" : iconMaterial);
        e.setIconModelData(iconModelData);
        entries.put(e.getId(), e);
        saveAll();
        return e;
    }
    public void deleteEntry(String id) { entries.remove(id); saveAll(); }
    /** true si une categorie possede au moins une icone custom dans le menu principal. */
    public boolean hasEntry(String categoryId) {
        for (ShopMenuEntry e : entries.values()) if (e.getCategoryId().equals(categoryId)) return true;
        return false;
    }

    /** Reinitialise tous les prix au prix initial (appele periodiquement). */
    public void resetAllPrices() {
        for (ShopCategory c : categories.values())
            for (ShopItem it : c.items().values()) it.resetPrice();
        saveAll();
    }

    // ---- Defaults : minerais / maison-deco / nourriture ----
    private void generateDefaults() {
        ShopCategory minerais = new ShopCategory("minerais", "&bMinerais");
        minerais.setIconMaterial("DIAMOND");
        addDefault(minerais, Material.COAL, 5);
        addDefault(minerais, Material.RAW_IRON, 12);
        addDefault(minerais, Material.RAW_COPPER, 8);
        addDefault(minerais, Material.RAW_GOLD, 20);
        addDefault(minerais, Material.IRON_INGOT, 18);
        addDefault(minerais, Material.GOLD_INGOT, 28);
        addDefault(minerais, Material.COPPER_INGOT, 10);
        addDefault(minerais, Material.REDSTONE, 6);
        addDefault(minerais, Material.LAPIS_LAZULI, 7);
        addDefault(minerais, Material.DIAMOND, 60);
        addDefault(minerais, Material.EMERALD, 45);
        addDefault(minerais, Material.QUARTZ, 9);
        addDefault(minerais, Material.NETHERITE_SCRAP, 250);
        addDefault(minerais, Material.AMETHYST_SHARD, 14);
        categories.put(minerais.getId(), minerais);

        ShopCategory maison = new ShopCategory("maison", "&eMaison & Deco");
        maison.setIconMaterial("CHEST");
        addDefault(maison, Material.CHEST, 15);
        addDefault(maison, Material.BARREL, 18);
        addDefault(maison, Material.OAK_LOG, 4);
        addDefault(maison, Material.OAK_PLANKS, 2);
        addDefault(maison, Material.STONE, 2);
        addDefault(maison, Material.STONE_BRICKS, 3);
        addDefault(maison, Material.GLASS, 4);
        addDefault(maison, Material.WHITE_WOOL, 5);
        addDefault(maison, Material.TORCH, 1);
        addDefault(maison, Material.LANTERN, 12);
        addDefault(maison, Material.FLOWER_POT, 6);
        addDefault(maison, Material.PAINTING, 10);
        addDefault(maison, Material.ITEM_FRAME, 8);
        addDefault(maison, Material.BOOKSHELF, 16);
        addDefault(maison, Material.SMOOTH_QUARTZ, 9);
        addDefault(maison, Material.BRICKS, 7);
        categories.put(maison.getId(), maison);

        ShopCategory nourriture = new ShopCategory("nourriture", "&6Nourriture");
        nourriture.setIconMaterial("COOKED_BEEF");
        addDefault(nourriture, Material.BREAD, 4);
        addDefault(nourriture, Material.COOKED_BEEF, 8);
        addDefault(nourriture, Material.COOKED_PORKCHOP, 8);
        addDefault(nourriture, Material.COOKED_CHICKEN, 6);
        addDefault(nourriture, Material.GOLDEN_APPLE, 80);
        addDefault(nourriture, Material.GOLDEN_CARROT, 14);
        addDefault(nourriture, Material.APPLE, 3);
        addDefault(nourriture, Material.CARROT, 2);
        addDefault(nourriture, Material.POTATO, 2);
        addDefault(nourriture, Material.WHEAT, 2);
        addDefault(nourriture, Material.SUGAR_CANE, 3);
        addDefault(nourriture, Material.CAKE, 25);
        addDefault(nourriture, Material.COOKIE, 2);
        categories.put(nourriture.getId(), nourriture);
    }

    private void addDefault(ShopCategory cat, Material mat, double price) {
        cat.addItem(ShopItem.create(new ItemStack(mat), price, defaultBuyStep(), defaultSellStep()));
    }
}
