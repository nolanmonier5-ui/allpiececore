package dev.serverforge.craft;

import dev.serverforge.ServerForgePlugin;
import dev.serverforge.craft.model.CraftCategory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Gere les categories/recettes de fabrication et leur persistance (craft.yml). */
public class CraftManager {
    private final ServerForgePlugin plugin;
    private final File file;
    private final Map<String, CraftCategory> categories = new LinkedHashMap<>();

    public CraftManager(ServerForgePlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "craft.yml");
    }

    public String mainTitle() { return plugin.getConfig().getString("craft.main-title", "&8Fabrication"); }
    public void setMainTitle(String t) { plugin.getConfig().set("craft.main-title", t); plugin.saveConfig(); }

    public void loadAll() {
        categories.clear();
        if (file.exists()) {
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection root = cfg.getConfigurationSection("categories");
            if (root != null)
                for (String id : root.getKeys(false)) {
                    ConfigurationSection s = root.getConfigurationSection(id);
                    if (s != null) categories.put(id, CraftCategory.load(id, s));
                }
        }
        if (categories.isEmpty()) {
            CraftCategory armes = new CraftCategory("armes", "&cArmes");
            armes.setIconMaterial("IRON_SWORD");
            categories.put("armes", armes);
            saveAll();
        }
        plugin.getLogger().info(categories.size() + " categorie(s) de craft chargee(s).");
    }

    public void saveAll() {
        YamlConfiguration cfg = new YamlConfiguration();
        ConfigurationSection root = cfg.createSection("categories");
        for (CraftCategory c : categories.values()) c.save(root.createSection(c.getId()));
        try { cfg.save(file); }
        catch (IOException e) { plugin.getLogger().severe("Echec sauvegarde craft.yml: " + e.getMessage()); }
    }

    public CraftCategory create(String id, String name) {
        String safe = id.toLowerCase().replaceAll("[^a-z0-9_-]", "_");
        if (categories.containsKey(safe)) return null;
        CraftCategory c = new CraftCategory(safe, name);
        categories.put(safe, c);
        saveAll();
        return c;
    }
    public void delete(String id) { categories.remove(id); saveAll(); }
    public CraftCategory get(String id) { return categories.get(id); }
    public List<CraftCategory> all() { return new ArrayList<>(categories.values()); }
}
