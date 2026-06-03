package dev.serverforge.craft.gui;

import dev.serverforge.ServerForgePlugin;
import dev.serverforge.craft.model.CraftCategory;
import dev.serverforge.util.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

/** Reglages d'une categorie de craft. */
public class CraftCategoryEditGui extends Gui {
    private final ServerForgePlugin plugin;
    private final CraftCategory cat;

    public CraftCategoryEditGui(ServerForgePlugin plugin, CraftCategory cat) {
        super("&8Categorie craft: &7" + cat.getName(), 3);
        this.plugin = plugin; this.cat = cat;
        build();
    }

    @Override
    public void build() {
        clear();
        setButton(10, new ItemBuilder(Material.NAME_TAG)
                .name("&aNom (icone): &f" + cat.getName()).lore("&7Nom affiche sur l'icone du menu.",
                      "&eClique pour renommer").build(),
                (p, c) -> plugin.chat().request(p, "Nom affiche sur l'icone :",
                        v -> { cat.setName(v); plugin.craft().saveAll(); reopen(p); }, () -> reopen(p)));

        setButton(11, new ItemBuilder(Material.OAK_SIGN)
                .name("&aTitre du menu: &f" + cat.getMenuTitle())
                .lore("&7Titre EN HAUT du menu de la categorie.",
                      "&8Independant du nom de l'icone.",
                      "&8Supporte les caracteres custom (police GUI).",
                      "&eClic gauche &7> definir",
                      "&cClic droit &7> = nom de l'icone").build(),
                (p, c) -> {
                    if (c == ClickType.RIGHT) { cat.setMenuTitle(""); plugin.craft().saveAll(); reopen(p); return; }
                    plugin.chat().request(p, "Titre du menu de la categorie :",
                            v -> { cat.setMenuTitle(v); plugin.craft().saveAll(); reopen(p); }, () -> reopen(p));
                });

        Material mat = Material.matchMaterial(cat.getIconMaterial());
        setButton(12, new ItemBuilder(mat != null ? mat : Material.ANVIL)
                .modelData(cat.getIconModelData()).hideAttributes()
                .name("&dIcone")
                .lore("&eClic gauche &7> choisir dans l'inventaire", "&cClic droit &7> reset").build(),
                (p, c) -> {
                    if (c == ClickType.RIGHT) { cat.setIconMaterial("ANVIL"); cat.setIconModelData(-1); plugin.craft().saveAll(); reopen(p); return; }
                    new ItemPicker(p, "&8Icone categorie",
                            s -> { cat.setIconFrom(s); plugin.craft().saveAll(); reopen(p); }, () -> reopen(p)).open(p);
                });

        setButton(14, new ItemBuilder(Material.ITEM_FRAME)
                .name("&aEmplacement: &f" + (cat.getSlot() < 0 ? "auto" : cat.getSlot())).build(),
                (p, c) -> new SlotPicker("&8Emplacement categorie", cat.getSlot(),
                        s -> { cat.setSlot(s); plugin.craft().saveAll(); reopen(p); }, () -> reopen(p)).open(p));

        setButton(16, new ItemBuilder(Material.BARRIER).name("&cSupprimer")
                .lore("&cShift + clic pour confirmer").build(),
                (p, c) -> { if (c == ClickType.SHIFT_LEFT || c == ClickType.SHIFT_RIGHT) {
                        plugin.craft().delete(cat.getId());
                        new CraftMainGui(plugin, p, true).open(p);
                    } });

        setButton(22, new ItemBuilder(Material.ARROW).name("&eRetour").build(),
                (p, c) -> new CraftMainGui(plugin, p, true).open(p));
    }
    private void reopen(Player p) { new CraftCategoryEditGui(plugin, cat).open(p); }
}
