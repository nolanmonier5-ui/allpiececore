package dev.serverforge.shop.gui;

import dev.serverforge.ServerForgePlugin;
import dev.serverforge.shop.model.ShopCategory;
import dev.serverforge.shop.model.ShopMenuEntry;
import dev.serverforge.util.Gui;
import dev.serverforge.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Menu principal du shop : des "icones" (entrees) qui ouvrent une categorie.
 * Plusieurs icones peuvent pointer vers la meme categorie (ex: 4 icones "Minerais").
 * Une categorie sans icone custom recoit une icone auto (pour ne pas la cacher).
 */
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

        // Construit la liste affichee : entrees custom + icones auto pour les categories sans entree
        List<Entry> shown = new ArrayList<>();
        for (ShopMenuEntry e : plugin.shop().entries()) {
            ShopCategory cat = plugin.shop().get(e.getCategoryId());
            if (cat == null) continue;
            shown.add(new Entry(e, cat));
        }
        for (ShopCategory cat : plugin.shop().all()) {
            if (!plugin.shop().hasEntry(cat.getId())) shown.add(new Entry(null, cat));
        }

        boolean[] used = new boolean[54];
        if (admin) { used[47] = true; used[48] = true; used[49] = true; used[50] = true; }

        for (Entry en : shown) {
            int slot = en.slot();
            if (slot >= 0 && slot <= 53 && !used[slot]) { place(en, slot); used[slot] = true; }
        }
        for (Entry en : shown) {
            if (en.slot() >= 0 && en.slot() <= 53) continue;
            int free = -1; for (int i = 0; i < 54; i++) if (!used[i]) { free = i; break; }
            if (free < 0) break;
            place(en, free); used[free] = true;
        }

        if (shown.isEmpty() && !admin)
            setButton(22, new ItemBuilder(Material.BARRIER).name("&cAucune categorie").build(), null);

        if (admin) {
            setButton(48, new ItemBuilder(Material.NETHER_STAR).name("&aCreer une categorie").glow(true).build(),
                    (p, c) -> plugin.chat().request(p, "Identifiant de la categorie (ex: minerais) :",
                            id -> plugin.chat().request(p, "Nom affiche :",
                                    nm -> { ShopCategory cat = plugin.shop().create(id, nm);
                                        if (cat == null) p.sendMessage(dev.serverforge.util.Text.color("&cExiste deja."));
                                        new ShopMainGui(plugin, p, true).open(p); },
                                    () -> new ShopMainGui(plugin, p, true).open(p)),
                            () -> new ShopMainGui(plugin, p, true).open(p)));

            setButton(50, new ItemBuilder(Material.ITEM_FRAME).name("&bIcones du menu principal").glow(true)
                    .lore("&7Ajoute autant d'icones que tu veux,",
                          "&7plusieurs peuvent ouvrir la meme categorie.",
                          "&eClique pour gerer").build(),
                    (p, c) -> new ShopEntriesGui(plugin).open(p));

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

    private void place(Entry en, int slot) {
        ShopCategory cat = en.cat;
        String name = en.entry != null ? en.entry.displayName(cat.getName()) : cat.getName();
        String mat = en.entry != null ? en.entry.getIconMaterial() : cat.getIconMaterial();
        int cmd = en.entry != null ? en.entry.getIconModelData() : cat.getIconModelData();
        Material m = Material.matchMaterial(mat);
        setButton(slot, new ItemBuilder(m != null ? m : Material.CHEST)
                .name("&6&l" + name)
                .modelData(cmd).hideAttributes()
                .lore("&7Items: &f" + cat.items().size(),
                      "",
                      "&e\u25B6 Clique pour ouvrir").build(),
                (p, c) -> new ShopCategoryGui(plugin, p, cat.getId(), admin).open(p));
    }

    /** Entree affichee : soit une icone custom (entry != null), soit une icone auto. */
    private record Entry(ShopMenuEntry entry, ShopCategory cat) {
        int slot() { return entry != null ? entry.getSlot() : cat.getSlot(); }
    }
}
