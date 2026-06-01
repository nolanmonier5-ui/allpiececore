package dev.serverforge.shop.gui;

import dev.serverforge.ServerForgePlugin;
import dev.serverforge.shop.model.ShopCategory;
import dev.serverforge.util.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

/** Reglages d'une categorie de shop : nom, icone, slot, suppression. */
public class ShopCategoryEditGui extends Gui {
    private final ServerForgePlugin plugin;
    private final ShopCategory cat;

    public ShopCategoryEditGui(ServerForgePlugin plugin, ShopCategory cat) {
        super("&8Categorie: &7" + cat.getName(), 3);
        this.plugin = plugin; this.cat = cat;
        build();
    }

    @Override
    public void build() {
        clear();
        setButton(10, new ItemBuilder(Material.NAME_TAG)
                .name("&aNom: &f" + cat.getName()).lore("&eClique pour renommer").build(),
                (p, c) -> plugin.chat().request(p, "Nouveau nom de la categorie :",
                        v -> { cat.setName(v); plugin.shop().saveAll(); reopen(p); }, () -> reopen(p)));

        Material iconMat = Material.matchMaterial(cat.getIconMaterial());
        setButton(12, new ItemBuilder(iconMat != null ? iconMat : Material.CHEST)
                .modelData(cat.getIconModelData()).hideAttributes()
                .name("&dIcone")
                .lore("&7Materiau: &f" + cat.getIconMaterial(),
                      "&eClic gauche &7> choisir dans l'inventaire",
                      "&cClic droit &7> reset (CHEST)").build(),
                (p, c) -> {
                    if (c == ClickType.RIGHT) { cat.setIconMaterial("CHEST"); cat.setIconModelData(-1); plugin.shop().saveAll(); reopen(p); return; }
                    new ItemPicker(p, "&8Icone de la categorie",
                            s -> { cat.setIconFrom(s); plugin.shop().saveAll(); reopen(p); }, () -> reopen(p)).open(p);
                });

        setButton(14, new ItemBuilder(Material.ITEM_FRAME)
                .name("&aEmplacement: &f" + (cat.getSlot() < 0 ? "auto" : cat.getSlot())).build(),
                (p, c) -> new SlotPicker("&8Emplacement categorie", cat.getSlot(),
                        s -> { cat.setSlot(s); plugin.shop().saveAll(); reopen(p); }, () -> reopen(p)).open(p));

        setButton(16, new ItemBuilder(Material.BARRIER).name("&cSupprimer la categorie")
                .lore("&cShift + clic pour confirmer").build(),
                (p, c) -> { if (c == ClickType.SHIFT_LEFT || c == ClickType.SHIFT_RIGHT) {
                        plugin.shop().delete(cat.getId());
                        new ShopMainGui(plugin, p, true).open(p);
                    } });

        setButton(22, new ItemBuilder(Material.ARROW).name("&eRetour").build(),
                (p, c) -> new ShopCategoryGui(plugin, p, cat.getId(), true).open(p));
    }
    private void reopen(Player p) { new ShopCategoryEditGui(plugin, cat).open(p); }
}
