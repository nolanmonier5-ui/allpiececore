package dev.serverforge.shop.gui;

import dev.serverforge.ServerForgePlugin;
import dev.serverforge.shop.model.ShopCategory;
import dev.serverforge.shop.model.ShopMenuEntry;
import dev.serverforge.util.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

/** Edition d'une icone du menu principal : nom, item, categorie, slot. */
public class ShopEntryEditGui extends Gui {
    private final ServerForgePlugin plugin;
    private final ShopMenuEntry entry;

    public ShopEntryEditGui(ServerForgePlugin plugin, ShopMenuEntry entry) {
        super("&8Icone du menu", 3);
        this.plugin = plugin; this.entry = entry;
        build();
    }

    @Override
    public void build() {
        clear();
        ShopCategory cat = plugin.shop().get(entry.getCategoryId());
        String catName = cat != null ? cat.getName() : entry.getCategoryId();

        setButton(10, new ItemBuilder(Material.NAME_TAG)
                .name("&aNom affiche: &f" + entry.displayName(catName))
                .lore("&7Texte sur l'icone (independant du nom de la categorie).",
                      "&eClic gauche &7> definir",
                      "&cClic droit &7> = nom de la categorie").build(),
                (p, c) -> {
                    if (c == ClickType.RIGHT) { entry.setDisplayName(""); plugin.shop().saveAll(); reopen(p); return; }
                    plugin.chat().request(p, "Nom affiche sur l'icone :",
                            v -> { entry.setDisplayName(v); plugin.shop().saveAll(); reopen(p); }, () -> reopen(p));
                });

        Material iconMat = Material.matchMaterial(entry.getIconMaterial());
        setButton(12, new ItemBuilder(iconMat != null ? iconMat : Material.CHEST)
                .modelData(entry.getIconModelData()).hideAttributes()
                .name("&dIcone")
                .lore("&7Materiau: &f" + entry.getIconMaterial(),
                      "&7CustomModelData: &f" + (entry.getIconModelData() < 0 ? "aucun" : entry.getIconModelData()),
                      "&eClic gauche &7> choisir dans l'inventaire",
                      "&cClic droit &7> reset (CHEST)").build(),
                (p, c) -> {
                    if (c == ClickType.RIGHT) { entry.setIconMaterial("CHEST"); entry.setIconModelData(-1); plugin.shop().saveAll(); reopen(p); return; }
                    new ItemPicker(p, "&8Icone du menu principal",
                            s -> { entry.setIconFrom(s); plugin.shop().saveAll(); reopen(p); }, () -> reopen(p)).open(p);
                });

        setButton(14, new ItemBuilder(Material.CHEST)
                .name("&aCategorie ouverte: &f" + catName)
                .lore("&7Id: &f" + entry.getCategoryId(), "&eClique pour changer").build(),
                (p, c) -> new ShopPickCategoryGui(plugin,
                        chosen -> { entry.setCategoryId(chosen.getId()); plugin.shop().saveAll(); reopen(p); },
                        () -> reopen(p)).open(p));

        setButton(16, new ItemBuilder(Material.ITEM_FRAME)
                .name("&aEmplacement: &f" + (entry.getSlot() < 0 ? "auto" : entry.getSlot()))
                .lore("&7Position de l'icone dans le menu principal.").build(),
                (p, c) -> new SlotPicker("&8Emplacement de l'icone", entry.getSlot(),
                        s -> { entry.setSlot(s); plugin.shop().saveAll(); reopen(p); }, () -> reopen(p)).open(p));

        setButton(22, new ItemBuilder(Material.BARRIER).name("&cRetour").build(),
                (p, c) -> new ShopEntriesGui(plugin).open(p));
    }
    private void reopen(Player p) { new ShopEntryEditGui(plugin, entry).open(p); }
}
