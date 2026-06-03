package dev.serverforge.craft.model;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Une recette de fabrication.
 * - result      : l'item produit (ItemStack complet : CMD, nom, lore conserves)
 * - ingredients : liste d'ItemStacks requis (type+CMD+quantite comptent)
 * - craftSeconds: duree de fabrication
 * - name        : nom affiche dans le menu (sinon nom de l'item)
 */
public class Recipe {
    private final String id;
    private String name;
    private String menuTitle = "";   // titre du menu de l'arme (vide = "&8" + name)
    private ItemStack result;
    private final List<ItemStack> ingredients = new ArrayList<>();
    private long craftSeconds = 0;
    private int slot = -1;

    public Recipe(String id, String name) { this.id = id; this.name = name; }
    public static Recipe create(String name) {
        return new Recipe(UUID.randomUUID().toString().substring(0, 8), name);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMenuTitle() { return menuTitle == null || menuTitle.isEmpty() ? "&8" + name : menuTitle; }
    public String getRawMenuTitle() { return menuTitle; }
    public void setMenuTitle(String t) { this.menuTitle = t == null ? "" : t; }
    public ItemStack getResult() { return result; }
    public void setResult(ItemStack result) { this.result = result; }
    public List<ItemStack> getIngredients() { return ingredients; }
    public long getCraftSeconds() { return craftSeconds; }
    public void setCraftSeconds(long s) { this.craftSeconds = Math.max(0, s); }
    public int getSlot() { return slot; }
    public void setSlot(int slot) { this.slot = slot; }

    public void save(ConfigurationSection sec) {
        sec.set("name", name);
        sec.set("menu-title", menuTitle);
        sec.set("result", result);
        sec.set("craft-seconds", craftSeconds);
        sec.set("slot", slot);
        ConfigurationSection ing = sec.createSection("ingredients");
        int i = 0;
        for (ItemStack s : ingredients) ing.set("i" + (i++), s);
    }

    public static Recipe load(String id, ConfigurationSection sec) {
        Recipe r = new Recipe(id, sec.getString("name", id));
        r.menuTitle = sec.getString("menu-title", "");
        r.result = sec.getItemStack("result");
        r.craftSeconds = sec.getLong("craft-seconds", 0);
        r.slot = sec.getInt("slot", -1);
        ConfigurationSection ing = sec.getConfigurationSection("ingredients");
        if (ing != null) {
            for (String key : ing.getKeys(false)) {
                ItemStack s = ing.getItemStack(key);
                if (s != null) r.ingredients.add(s);
            }
        }
        return r;
    }
}
