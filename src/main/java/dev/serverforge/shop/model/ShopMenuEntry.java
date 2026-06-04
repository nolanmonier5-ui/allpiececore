package dev.serverforge.shop.model;

import dev.serverforge.util.ItemUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Une icone du menu principal du shop. Plusieurs entrees peuvent pointer vers la
 * MEME categorie (ex: 4 icones "Minerais" a des emplacements differents).
 * Le nom affiche, l'item, le CMD et le slot sont propres a l'entree.
 */
public class ShopMenuEntry {
    private final String id;
    private String categoryId;
    private String displayName;     // vide = utilise le nom de la categorie
    private String iconMaterial = "CHEST";
    private int iconModelData = -1;
    private int slot = -1;

    public ShopMenuEntry(String id, String categoryId, String displayName) {
        this.id = id;
        this.categoryId = categoryId;
        this.displayName = displayName == null ? "" : displayName;
    }
    public static ShopMenuEntry create(String categoryId, String displayName) {
        return new ShopMenuEntry(UUID.randomUUID().toString().substring(0, 8), categoryId, displayName);
    }

    public String getId() { return id; }
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String c) { this.categoryId = c; }
    public String getRawDisplayName() { return displayName; }
    public String displayName(String fallback) { return displayName == null || displayName.isEmpty() ? fallback : displayName; }
    public void setDisplayName(String n) { this.displayName = n == null ? "" : n; }
    public String getIconMaterial() { return iconMaterial; }
    public void setIconMaterial(String m) { this.iconMaterial = m; }
    public int getIconModelData() { return iconModelData; }
    public void setIconModelData(int d) { this.iconModelData = d; }
    public int getSlot() { return slot; }
    public void setSlot(int s) { this.slot = s; }

    public void setIconFrom(ItemStack stack) {
        iconMaterial = stack.getType().name();
        iconModelData = ItemUtil.readModelData(stack);
    }

    public void save(ConfigurationSection sec) {
        sec.set("category", categoryId);
        sec.set("name", displayName);
        sec.set("icon.material", iconMaterial);
        if (iconModelData >= 0) sec.set("icon.model-data", iconModelData);
        sec.set("slot", slot);
    }
    public static ShopMenuEntry load(String id, ConfigurationSection sec) {
        ShopMenuEntry e = new ShopMenuEntry(id, sec.getString("category", ""), sec.getString("name", ""));
        e.iconMaterial = sec.getString("icon.material", "CHEST");
        e.iconModelData = sec.getInt("icon.model-data", -1);
        e.slot = sec.getInt("slot", -1);
        return e;
    }
}
