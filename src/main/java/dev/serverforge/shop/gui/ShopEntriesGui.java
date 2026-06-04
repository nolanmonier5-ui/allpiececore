package dev.serverforge.shop.gui;

import dev.serverforge.ServerForgePlugin;
import dev.serverforge.shop.model.ShopCategory;
import dev.serverforge.shop.model.ShopMenuEntry;
import dev.serverforge.util.Gui;
import dev.serverforge.util.ItemBuilder;
import dev.serverforge.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.List;

/** Liste et gestion des icones du menu principal du shop. */
public class ShopEntriesGui extends Gui {
    private final ServerForgePlugin plugin;

    public ShopEntriesGui(ServerForgePlugin plugin) {
        super("&8Icones du menu principal", 6);
        this.plugin = plugin;
        build();
    }

    @Override
    public void build() {
        clear();
        List<ShopMenuEntry> list = plugin.shop().entries();
        for (int i = 0; i < list.size() && i < 45; i++) {
            ShopMenuEntry e = list.get(i);
            ShopCategory cat = plugin.shop().get(e.getCategoryId());
            String catName = cat != null ? cat.getName() : e.getCategoryId();
            Material mat = Material.matchMaterial(e.getIconMaterial());
            setButton(i, new ItemBuilder(mat != null ? mat : Material.CHEST)
                    .name("&6" + e.displayName(catName))
                    .modelData(e.getIconModelData()).hideAttributes()
                    .lore("&7Ouvre: &f" + catName + " &8(" + e.getCategoryId() + ")",
                          "&7Slot: &f" + (e.getSlot() < 0 ? "auto" : e.getSlot()),
                          "",
                          "&eClic gauche &7> editer",
                          "&cShift + droit &7> supprimer").build(),
                    (p, c) -> {
                        if (c == ClickType.SHIFT_RIGHT) {
                            plugin.shop().deleteEntry(e.getId());
                            p.sendMessage(Text.color("&cIcone supprimee."));
                            build();
                        } else new ShopEntryEditGui(plugin, e).open(p);
                    });
        }

        setButton(49, new ItemBuilder(Material.NETHER_STAR).name("&aAjouter une icone").glow(true)
                .lore("&7Choisis la categorie a ouvrir,",
                      "&7puis personnalise l'icone (nom, item, slot).").build(),
                (p, c) -> new ShopPickCategoryGui(plugin, cat -> {
                    ShopMenuEntry e = plugin.shop().createEntry(cat.getId(), cat.getName(),
                            cat.getIconMaterial(), cat.getIconModelData());
                    new ShopEntryEditGui(plugin, e).open(p);
                }, () -> new ShopEntriesGui(plugin).open(p)).open(p));

        setButton(53, new ItemBuilder(Material.BARRIER).name("&cRetour").build(),
                (p, c) -> new ShopMainGui(plugin, p, true).open(p));
    }
}
