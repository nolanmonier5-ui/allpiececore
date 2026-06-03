package dev.serverforge.craft.gui;

import dev.serverforge.ServerForgePlugin;
import dev.serverforge.craft.model.CraftCategory;
import dev.serverforge.util.Gui;
import dev.serverforge.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/** Menu principal de fabrication : les categories. */
public class CraftMainGui extends Gui {
    private final ServerForgePlugin plugin;
    private final boolean admin;

    public CraftMainGui(ServerForgePlugin plugin, Player viewer, boolean admin) {
        super(plugin.craft().mainTitle(), 6);
        this.plugin = plugin; this.admin = admin;
        build();
    }

    @Override
    public void build() {
        clear();
        boolean[] used = new boolean[54];
        if (admin) { used[45] = true; used[49] = true; used[53] = true; }
        for (CraftCategory cat : plugin.craft().all()) {
            int slot = cat.getSlot();
            if (slot >= 0 && slot <= 53 && !used[slot]) { place(cat, slot); used[slot] = true; }
        }
        for (CraftCategory cat : plugin.craft().all()) {
            if (cat.getSlot() >= 0 && cat.getSlot() <= 53) continue;
            int free = -1; for (int i = 0; i < 54; i++) if (!used[i]) { free = i; break; }
            if (free < 0) break;
            place(cat, free); used[free] = true;
        }
        if (admin) {
            setButton(49, new ItemBuilder(Material.NETHER_STAR).name("&aCreer une categorie").glow(true).build(),
                    (p, c) -> plugin.chat().request(p, "Identifiant de la categorie (ex: epees) :",
                            id -> plugin.chat().request(p, "Nom affiche :",
                                    name -> { CraftCategory cat = plugin.craft().create(id, name);
                                        if (cat == null) p.sendMessage(dev.serverforge.util.Text.color("&cExiste deja."));
                                        new CraftMainGui(plugin, p, true).open(p); },
                                    () -> new CraftMainGui(plugin, p, true).open(p)),
                            () -> new CraftMainGui(plugin, p, true).open(p)));
            setButton(53, new ItemBuilder(Material.OAK_SIGN)
                    .name("&aTitre du menu principal")
                    .lore("&7Actuel: &f" + plugin.craft().mainTitle(),
                          "&8Supporte les caracteres custom.",
                          "&eClique pour modifier").build(),
                    (p, c) -> plugin.chat().request(p, "Nouveau titre du menu principal de la fabrication :",
                            v -> { plugin.craft().setMainTitle(v); new CraftMainGui(plugin, p, true).open(p); },
                            () -> new CraftMainGui(plugin, p, true).open(p)));
            setButton(45, new ItemBuilder(Material.ARROW)
                    .name("&aFleche retour")
                    .lore("&7Item, modeldata, nom, slot du bouton retour",
                          "&7des menus de categorie (shop + craft).",
                          "&eClique pour configurer").build(),
                    (p, c) -> new dev.serverforge.util.BackButtonGui(plugin,
                            () -> new CraftMainGui(plugin, p, true).open(p)).open(p));
        }
    }

    private void place(CraftCategory cat, int slot) {
        Material mat = Material.matchMaterial(cat.getIconMaterial());
        setButton(slot, new ItemBuilder(mat != null ? mat : Material.ANVIL)
                .name("&6&l" + cat.getName())
                .modelData(cat.getIconModelData()).hideAttributes()
                .lore("&7Recettes: &f" + cat.recipes().size(),
                      "&e\u25B6 Clique pour ouvrir",
                      admin ? "&dClic droit > editer la categorie" : "").build(),
                (p, c) -> {
                    if (admin && c.isRightClick()) new CraftCategoryEditGui(plugin, cat).open(p);
                    else new CraftCategoryGui(plugin, p, cat.getId(), admin).open(p);
                });
    }
}
