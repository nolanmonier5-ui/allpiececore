package dev.serverforge.shop.model;

import dev.serverforge.util.ItemUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Categorie du shop : un menu d'items dynamiques. */
public class ShopCategory {
    private final String id;
    private String name;
    private String menuTitle = "";   // titre du menu de la categorie (vide = utilise 'name')
    private String iconMaterial = "CHEST";
    private int iconModelData = -1;
    private int slot = -1;
    private final Map<String, ShopItem> items = new LinkedHashMap<>();

    public ShopCategory(String id, String name) { this.id = id; this.name = name; }

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
    public Map<String, ShopItem> items() { return items; }
    public List<ShopItem> itemList() { return new ArrayList<>(items.values()); }

    public void addItem(ShopItem it) { items.put(it.getId(), it); }
    public void removeItem(String id) { items.remove(id); }

    public void save(ConfigurationSection sec) {
        sec.set("name", name);
        sec.set("menu-title", menuTitle);
        sec.set("icon.material", iconMaterial);
        if (iconModelData >= 0) sec.set("icon.model-data", iconModelData);
        sec.set("slot", slot);
        ConfigurationSection is = sec.createSection("items");
        for (ShopItem it : items.values()) it.save(is.createSection(it.getId()));
    }

    public static ShopCategory load(String id, ConfigurationSection sec) {
        ShopCategory c = new ShopCategory(id, sec.getString("name", id));
        c.menuTitle = sec.getString("menu-title", "");
        c.iconMaterial = sec.getString("icon.material", "CHEST");
        c.iconModelData = sec.getInt("icon.model-data", -1);
        c.slot = sec.getInt("slot", -1);
        ConfigurationSection is = sec.getConfigurationSection("items");
        if (is != null) {
            for (String key : is.getKeys(false)) {
                ShopItem it = ShopItem.load(key, is.getConfigurationSection(key));
                if (it != null) c.items.put(key, it);
            }
        }
        return c;
    }

    public void setIconFrom(ItemStack stack) {
        iconMaterial = stack.getType().name();
        iconModelData = ItemUtil.readModelData(stack);
    }
}
