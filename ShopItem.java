package dev.serverforge.craft.model;

import dev.serverforge.util.ItemUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Categorie d'armes/recettes : un menu de recettes. */
public class CraftCategory {
    private final String id;
    private String name;
    private String menuTitle = "";
    private String iconMaterial = "ANVIL";
    private int iconModelData = -1;
    private int slot = -1;
    private final Map<String, Recipe> recipes = new LinkedHashMap<>();

    public CraftCategory(String id, String name) { this.id = id; this.name = name; }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMenuTitle() { return menuTitle == null || menuTitle.isEmpty() ? name : menuTitle; }
    public String getRawMenuTitle() { return menuTitle; }
    public void setMenuTitle(String t) { this.menuTitle = t == null ? "" : t; }
    public String getIconMaterial() { return iconMaterial; }
    public void setIconMaterial(String m) { this.iconMaterial = m; }
    public int getIconModelData() { return iconModelData; }
    public void setIconModelData(int d) { this.iconModelData = d; }
    public int getSlot() { return slot; }
    public void setSlot(int slot) { this.slot = slot; }
    public Map<String, Recipe> recipes() { return recipes; }
    public List<Recipe> recipeList() { return new ArrayList<>(recipes.values()); }
    public void addRecipe(Recipe r) { recipes.put(r.getId(), r); }
    public void removeRecipe(String id) { recipes.remove(id); }

    public void setIconFrom(ItemStack stack) {
        iconMaterial = stack.getType().name();
        iconModelData = ItemUtil.readModelData(stack);
    }

    public void save(ConfigurationSection sec) {
        sec.set("name", name);
        sec.set("menu-title", menuTitle);
        sec.set("icon.material", iconMaterial);
        if (iconModelData >= 0) sec.set("icon.model-data", iconModelData);
        sec.set("slot", slot);
        ConfigurationSection rs = sec.createSection("recipes");
        for (Recipe r : recipes.values()) r.save(rs.createSection(r.getId()));
    }

    public static CraftCategory load(String id, ConfigurationSection sec) {
        CraftCategory c = new CraftCategory(id, sec.getString("name", id));
        c.menuTitle = sec.getString("menu-title", "");
        c.iconMaterial = sec.getString("icon.material", "ANVIL");
        c.iconModelData = sec.getInt("icon.model-data", -1);
        c.slot = sec.getInt("slot", -1);
        ConfigurationSection rs = sec.getConfigurationSection("recipes");
        if (rs != null) {
            for (String key : rs.getKeys(false)) {
                Recipe r = Recipe.load(key, rs.getConfigurationSection(key));
                if (r != null) c.recipes.put(key, r);
            }
        }
        return c;
    }
}
