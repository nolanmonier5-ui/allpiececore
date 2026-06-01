package dev.serverforge.shop.gui;

import dev.serverforge.ServerForgePlugin;
import dev.serverforge.shop.model.ShopCategory;
import dev.serverforge.util.Gui;
import dev.serverforge.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/** Menu principal du shop : les categories. */
public class ShopMainGui extends Gui {
    private final ServerForgePlugin plugin;
    private final boolean admin;

    public ShopMainGui(ServerForgePlugin plugin, Player viewer, boolean admin) {
        super(plugin.shop().mainTitle(), 6);
        this.plugin = plugin;
        this.admin = admin;
        build();
    }

    @Override
    public void build() {
        clear();
        boolean[] used = new boolean[54];
        for (ShopCategory cat : plugin.shop().all()) {
            int slot = cat.getSlot();
            if (slot >= 0 && slot <= 53 && !used[slot]) { place(cat, slot); used[slot] = true; }
        }
        for (ShopCategory cat : plugin.shop().all()) {
            if (cat.getSlot() >= 0 && cat.getSlot() <= 53) continue;
            int free = -1; for (int i = 0; i < 54; i++) if (!used[i]) { free = i; break; }
            if (free < 0) break;
            place(cat, free); used[free] = true;
        }
        if (plugin.shop().all().isEmpty())
            setButton(22, new ItemBuilder(Material.BARRIER).name("&cAucune categorie").build(), null);
    }

    private void place(ShopCategory cat, int slot) {
        Material mat = Material.matchMaterial(cat.getIconMaterial());
        setButton(slot, new ItemBuilder(mat != null ? mat : Material.CHEST)
                .name("&6&l" + cat.getName())
                .modelData(cat.getIconModelData())
                .hideAttributes()
                .lore("&7Items: &f" + cat.items().size(),
                      "",
                      "&e\u25B6 Clique pour ouvrir").build(),
                (p, c) -> new ShopCategoryGui(plugin, p, cat.getId(), admin).open(p));
    }
}
