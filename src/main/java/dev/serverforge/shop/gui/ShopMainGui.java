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
        if (admin) { used[47] = true; used[48] = true; used[49] = true; used[50] = true; }
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
        if (plugin.shop().all().isEmpty() && !admin)
            setButton(22, new ItemBuilder(Material.BARRIER).name("&cAucune categorie").build(), null);

        if (admin) {
            setButton(48, new ItemBuilder(Material.NETHER_STAR).name("&aCreer une categorie").glow(true).build(),
                    (p, c) -> plugin.chat().request(p, "Identifiant de la categorie (ex: minerais) :",
                            id -> plugin.chat().request(p, "Nom affiche sur l'icone :",
                                    name -> { ShopCategory cat = plugin.shop().create(id, name);
                                        if (cat == null) p.sendMessage(dev.serverforge.util.Text.color("&cExiste deja."));
                                        new ShopMainGui(plugin, p, true).open(p); },
                                    () -> new ShopMainGui(plugin, p, true).open(p)),
                            () -> new ShopMainGui(plugin, p, true).open(p)));
            setButton(49, new ItemBuilder(Material.OAK_SIGN)
                    .name("&aTitre du menu principal")
                    .lore("&7Actuel: &f" + plugin.shop().mainTitle(),
                          "&8Supporte les caracteres custom.",
                          "&eClique pour modifier").build(),
                    (p, c) -> plugin.chat().request(p, "Nouveau titre du menu principal du shop :",
                            v -> { plugin.shop().setMainTitle(v); new ShopMainGui(plugin, p, true).open(p); },
                            () -> new ShopMainGui(plugin, p, true).open(p)));
            setButton(47, new ItemBuilder(Material.ARROW)
                    .name("&aFleche retour")
                    .lore("&7Item, modeldata, nom, slot du bouton retour",
                          "&7des menus de categorie (shop + craft).",
                          "&eClique pour configurer").build(),
                    (p, c) -> new dev.serverforge.util.BackButtonGui(plugin,
                            () -> new ShopMainGui(plugin, p, true).open(p)).open(p));
        }
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
